package domain.journey

import domain.barrier.Barrier

trait JourneyRepositoryAlgebra[F[_]] {
  def addJourney(cardNumber: Int, barrier: Barrier): F[Unit]

  def getMostRecentTubeBarrier(cardNumber: Int): F[Option[Barrier]]

  def journeyHistory(cardNumber: Int): F[List[Barrier]]
}
