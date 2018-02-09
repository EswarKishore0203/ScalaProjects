package domain.payment

import domain.card.Card

trait PaymentRepositoryAlgebra[F[_]] {
  def payment(card: Card, charge: Double): F[Card]
}
