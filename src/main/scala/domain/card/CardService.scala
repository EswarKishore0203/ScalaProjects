package domain.card

import cats._
import cats.data._
import domain.barrier.Type.{Bus, Tube}
import domain.barrier.{Barrier, BarrierService}
import domain.fare.CalculatorFareService
import domain.journey.JourneyService
import domain.payment.PaymentService

class CardService[F[_]](barrierService: BarrierService[F],
                        paymentService: PaymentService[F],
                        journeyService: JourneyService[F],
                        calculateFareService: CalculatorFareService[F],
                        cardRepositoryAlgebra: CardRepositoryAlgebra[F]) {

  import cats.syntax.all._

  /**
    * Create a card and load a balance on card
    * @param amount: Double
    * @return Card
    */
  def createCard(amount: Double): Card = cardRepositoryAlgebra.createCard("anonymous", amount)

  /**
    * This function is responsible to making a Tube journey
    * @param current: Barrier
    * @param card: Card
    * @param M Monad[F]
    * @return `F[Either[domain.ValidationError, (Card, Barrier)]]`
    */
  def makeTubeJourney(current: Barrier,
                      card: Card)(implicit M: Monad[F]): F[Either[domain.ValidationError, (Card, Barrier)]] = {
    barrierService.getMostRecentTubeBarrier(card).flatMap {
      case Some(comingFrom) =>
        val paymentStatement = for {
          _ <- barrierService.attemptToPassBarrier(current, comingFrom, card)
          chargedAtBarrier <- EitherT.liftF[F, domain.ValidationError, Barrier](calculateFareService.calculateTubeFare(current, Some(comingFrom)))
          transaction <- EitherT.liftF[F, domain.ValidationError, (Card, Barrier)](paymentService.makePayment(chargedAtBarrier, card))
        } yield transaction
        paymentStatement.value
      case None =>
        val transaction = for {
          _ <- barrierService.attemptToPassBarrier(current, card)
          chargedAtBarrier <- EitherT.liftF[F, domain.ValidationError, Barrier](calculateFareService.calculateTubeFare(current, None))
          updatedCard <- EitherT.liftF[F, domain.ValidationError, (Card, Barrier)](paymentService.makePayment(chargedAtBarrier, card))
        } yield updatedCard
        transaction.value
    }
  }

  /**
    * This function is responsible to making a bus journey
    * @param current: Barrier
    * @param card: Card
    * @param M: Monad[F]
    * @return `F[Either[domain.ValidationError, (Card, Barrier)]]`
    */
  def makeBusJourney(current: Barrier,
                     card: Card)(implicit M: Monad[F]): F[Either[domain.ValidationError, (Card, Barrier)]] = {

    val paymentStatement = for {
      _ <- barrierService.attemptToPassBarrier(current, card)
      chargedAtBarrier <- EitherT.liftF[F, domain.ValidationError, Barrier](calculateFareService.calculateBusFare(current))
      transaction <- EitherT.liftF[F, domain.ValidationError, (Card, Barrier)](paymentService.makePayment(chargedAtBarrier, card))
    } yield transaction
    paymentStatement.value
  }

  /**
    * This is responsible to making kind of journey
    * @param barrier: Barrier
    * @param card: Card
    * @param M: Monad[F]
    * @return `F[Either[domain.ValidationError, (Card, Barrier)]]``
    */
  def makeJourney(barrier: Barrier,
                  card: Card)(implicit M: Monad[F]): F[Either[domain.ValidationError, (Card, Barrier)]] = barrier.`type` match {
    case Tube => makeTubeJourney(barrier, card)
    case Bus => makeBusJourney(barrier, card)
  }

  /**
    * This function will get card's history
    * @param card: Card
    * @return `F[List[Barrier]]`
    */
  def transactionHistory(card: Card): F[List[Barrier]] =
    journeyService.journeyHistory(card.number)
}

object CardService {
  def apply[F[_]](barrierService: BarrierService[F],
                  paymentService: PaymentService[F],
                  journeyService: JourneyService[F],
                  fareCalculatorService: CalculatorFareService[F],
                  cardRepositoryAlgebra: CardRepositoryAlgebra[F]): CardService[F] =
    new CardService(barrierService, paymentService,
      journeyService, fareCalculatorService, cardRepositoryAlgebra)
}
