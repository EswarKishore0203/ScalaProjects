package domain.barrier

import domain.ValidationError
import domain.card.Card

trait BarrierValidationAlgebra[F[_]] {
  def verifyTripIsValid(current: Barrier,
                        previous: Barrier,
                        card: Card): F[Either[ValidationError, Unit]]

  def minimumBalanceForTravel(card: Card, `type`: Type): F[Either[ValidationError, Unit]]

}
