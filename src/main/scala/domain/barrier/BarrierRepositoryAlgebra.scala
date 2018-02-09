package domain.barrier

import domain.card.Card

trait BarrierRepositoryAlgebra[F[_]] {
  def passBarrier(barrier: Barrier, card: Card): F[Card]
}
