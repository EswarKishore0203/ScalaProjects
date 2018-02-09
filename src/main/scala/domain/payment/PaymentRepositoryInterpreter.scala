package domain.payment

import cats.Monad
import domain.card.Card

class PaymentRepositoryInterpreter[F[_] : Monad] extends PaymentRepositoryAlgebra[F] {

  import cats.syntax.all._

  override def payment(card: Card, charge: Double): F[Card] =
    card.update(charge).pure[F]
}

object PaymentRepositoryInterpreter {
  def apply[F[_] : Monad](): PaymentRepositoryAlgebra[F] = new PaymentRepositoryInterpreter[F]()
}
