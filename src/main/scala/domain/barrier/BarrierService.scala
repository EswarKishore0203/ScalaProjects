package domain.barrier

import cats.Monad
import cats.data.EitherT
import domain.ValidationError
import domain.card.Card
import domain.journey.JourneyService

class BarrierService[F[_]](ja: JourneyService[F],
                           validationAlgebra: BarrierValidationAlgebra[F]) {

  /**
    * Get most recent journey
    *
    * @param card : Card
    * @return `F[Option[Barrier]]`
    */
  def getMostRecentTubeBarrier(card: Card): F[Option[Barrier]] =
    ja.getMostRecentTubeBarrier(card.number)


  /**
    * This function will try to pass tube barrier. If user has balance is less than required balance then It will not
    * allow to enter.
    * @param current: Barrier
    * @param previous: Barrier
    * @param card: Card
    * @param M: Monad[F]
    * @return EitherT[F, ValidationError, Unit]
    */
  def attemptToPassBarrier(current: Barrier,
                           previous: Barrier,
                           card: Card)(implicit M: Monad[F]): EitherT[F, ValidationError, Unit] = {
    for {
      _ <- EitherT(validationAlgebra.minimumBalanceForTravel(card, current.`type`))
      _ <- EitherT(validationAlgebra.verifyTripIsValid(current, previous, card))
    } yield (): Unit
  }

  /**
    * This function will try to allow a user to enter in the bus. If user has less balance than required fare then it will
    * not allow a user to enter.
    * @param current: Barrier
    * @param card: Card
    * @return EitherT[F, ValidationError, Unit]
    */
  def attemptToPassBarrier(current: Barrier,
                           card: Card): EitherT[F, ValidationError, Unit] = EitherT {
    validationAlgebra.minimumBalanceForTravel(card, current.`type`)
  }
}

object BarrierService {
  def apply[F[_]](ja: JourneyService[F],
                  validationAlgebra: BarrierValidationAlgebra[F]): BarrierService[F] =
    new BarrierService(ja, validationAlgebra)
}
