package domain.barrier

import cats.Monad
import domain.barrier.Type.{Bus, Tube}
import domain.{BarrierInError, BarrierOutError, MinimumBalanceError, ValidationError}
import domain.card.Card
import domain.journey.JourneyRepositoryAlgebra
import BarrierValidationInterpreter.MAXIMUM_FARE_FOR_TRAVEL_TYPE

class BarrierValidationInterpreter[F[_] : Monad](ja: JourneyRepositoryAlgebra[F]) extends BarrierValidationAlgebra[F] {

  import cats.syntax.all._

  /**
    * This function will validate user's last journey/current journey valid or not
    * User can not tap in when he/she didn't tapped out
    *
    * @param currentBarrier: Barrier
    * @param previousBarrier: Barrier
    * @param card: Card
    * @return `F[Either[ValidationError, Unit]]`
    */
  def verifyTripIsValid(currentBarrier: Barrier,
                        previousBarrier: Barrier, card: Card): F[Either[ValidationError, Unit]] = {
    val trip =
      if ((currentBarrier.direction == Direction.OUT) && (previousBarrier.direction == Direction.OUT))
        Left(BarrierOutError)
      else if ((currentBarrier.direction == Direction.IN) && (previousBarrier.direction == Direction.IN))
        Left(BarrierInError)
      else Right((): Unit)
    trip.pure[F]
  }

  /**
    * This function will check minimum balance for travel
    * @param card: Card
    * @param `type`: Type
    * @return `F[Either[ValidationError, Unit]]`
    */
  override def minimumBalanceForTravel(card: Card, `type`: Type): F[Either[ValidationError, Unit]] = {
    MAXIMUM_FARE_FOR_TRAVEL_TYPE.
      get(`type`).filter(f => card.balance >= f)
      .fold(Either.left[ValidationError, Unit](MinimumBalanceError).pure[F]) {
        _ => Either.right[ValidationError, Unit]((): Unit).pure[F]
      }
  }
}

object BarrierValidationInterpreter {
  val MAXIMUM_FARE_FOR_TRAVEL_TYPE = Map(Tube -> 3.2, Bus -> 1.8)

  def apply[F[_] : Monad](ja: JourneyRepositoryAlgebra[F]): BarrierValidationAlgebra[F] =
    new BarrierValidationInterpreter(ja)
}
