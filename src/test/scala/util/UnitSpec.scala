package util

import cats.{Applicative, Monad}
import domain.barrier.Type.{Bus, Tube}
import domain.barrier._
import domain.card.{Card, CardService}
import domain.fare.CalculatorFareService
import domain.journey.{JourneyRepositoryAlgebra, JourneyService}
import domain.payment.{PaymentRepositoryAlgebra, PaymentRepositoryInterpreter, PaymentService}
import domain.zone.FindZonesService
import infrastructure.inmemory.card.CardRepositoryInMemoryInterpreter
import infrastructure.inmemory.journey.JourneyRepositoryInMemoryInterpreter
import infrastructure.inmemory.zone.ZoneRepositoryInMemoryInterpreter
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

trait UnitSpec extends fixture.FlatSpecLike
  with Matchers with OptionValues with
  EitherValues with
  Inside with Inspectors with ScalaFutures {

  class SetUp[F[_] : Applicative : Monad]() {
    val journeyRepo: JourneyRepositoryAlgebra[F] = JourneyRepositoryInMemoryInterpreter[F]()
    val barrierValidation: BarrierValidationAlgebra[F] = BarrierValidationInterpreter[F](journeyRepo)
    val paymentRepo: PaymentRepositoryAlgebra[F] = PaymentRepositoryInterpreter[F]()
    val zoneRepo: ZoneRepositoryInMemoryInterpreter[F] = ZoneRepositoryInMemoryInterpreter[F]()
    val cardRepo: CardRepositoryInMemoryInterpreter[F] = CardRepositoryInMemoryInterpreter[F]()
    val journeyService: JourneyService[F] = JourneyService[F](journeyRepo)
    val barrierService: BarrierService[F] = BarrierService[F](journeyService, barrierValidation)
    val paymentService: PaymentService[F] = PaymentService[F](paymentRepo, journeyRepo, cardRepo)
    val findZonesService: FindZonesService[F] = FindZonesService[F](zoneRepo)
    val faceService: CalculatorFareService[F] = CalculatorFareService[F](findZonesService)
    val cardService: CardService[F] = CardService[F](barrierService, paymentService, journeyService, faceService, cardRepo)

    def tube(code: String, direction: Direction.Value): Barrier = Barrier(code, Tube, direction, 0)

    def bus(code: String): Barrier = Barrier(code, Bus, Direction.IN, 0)

    def createCard(amount: Double): Card = cardService.createCard(amount)
  }

}
