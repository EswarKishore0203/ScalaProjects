package domain.journey

import domain.barrier.Barrier

class JourneyService[F[_]](journeyAlgebra: JourneyRepositoryAlgebra[F]) {
  def addJourney(cardNumber: Int,
                 barrier: Barrier): F[Unit] = journeyAlgebra.addJourney(cardNumber, barrier)

  def getMostRecentTubeBarrier(cardNumber: Int): F[Option[Barrier]] =
    journeyAlgebra.getMostRecentTubeBarrier(cardNumber)

  def journeyHistory(cardNumber: Int): F[List[Barrier]] =
    journeyAlgebra.journeyHistory(cardNumber)
}

object JourneyService {
  def apply[F[_]](journeyAlgebra: JourneyRepositoryAlgebra[F]): JourneyService[F] = new JourneyService(journeyAlgebra)
}
