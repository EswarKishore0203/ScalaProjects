package domain.payment

import cats.Monad
import cats.implicits._
import domain.barrier.Barrier
import domain.card.{Card, CardRepositoryAlgebra}
import domain.journey.JourneyRepositoryAlgebra

class PaymentService[F[_]](paymentAlgebra: PaymentRepositoryAlgebra[F],
                           jr: JourneyRepositoryAlgebra[F],
                           cardRepositoryAlgebra: CardRepositoryAlgebra[F]) {

  def makePayment(barrier: Barrier, card: Card)(implicit M: Monad[F]): F[(Card, Barrier)] = {
    for {
      updatedCard <- paymentAlgebra.payment(card, barrier.charge)
      _ <- jr.addJourney(updatedCard.number, barrier)
      _ <- cardRepositoryAlgebra.updateBalance(updatedCard.number, barrier.charge)
    } yield (updatedCard, barrier)
  }

}

object PaymentService {
  def apply[F[_]](paymentAlgebra: PaymentRepositoryAlgebra[F],
                  jr: JourneyRepositoryAlgebra[F],
                  cardRepositoryAlgebra: CardRepositoryAlgebra[F]): PaymentService[F] = new PaymentService(paymentAlgebra, jr, cardRepositoryAlgebra)
}
