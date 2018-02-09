package domain.fare

import cats.Monad
import domain.barrier.{Barrier, Direction}
import domain.fare.CalculatorFareService._
import domain.zone.FindZonesService

class CalculatorFareService[F[_] : Monad](zoneService: FindZonesService[F]) {

  import cats.syntax.all._

  /**
    * Calculate fare for bus travel
    * @param barrier: Barrier
    * @return F[Barrier] return barrier with charged on travel
    */
  def calculateBusFare(barrier: Barrier): F[Barrier] = barrier.copy(charge = BUS_COST).pure[F]

  /**
    * Calculate fare for Tube travel
    * @param barrier
    * @param previousBarrier
    * @return
    */
  def calculateTubeFare(barrier: Barrier,
                        previousBarrier: Option[Barrier]): F[Barrier] = {
    if (barrier.direction == Direction.IN) barrier.copy(charge = MAX_COST).pure[F]
    else {
      previousBarrier match {
        case Some(pB) =>
          val previousBarrierZones = zoneService.getZonesByCode(pB.code)
          val barrierZones = zoneService.getZonesByCode(barrier.code)
          for {
            pZones <- previousBarrierZones
            bZones <- barrierZones
            minZonesCrossed <- zoneService.findMinimumNumberOfZoneCrossed(pZones, bZones).pure[F]
            zoneOneCrossed <- zoneService.haveYouCrossedZoneOne(pZones, bZones, minZonesCrossed).pure[F]
          } yield CrossedZone
            .getCost(minZonesCrossed, zoneOneCrossed)
            .map(c => barrier.copy(charge = c.charge - MAX_COST))
            .getOrElse(barrier.copy(charge = MAX_COST))
        case None => barrier.copy(charge = MAX_COST).pure
      }
    }

  }

}

object CalculatorFareService {
  val MAX_COST = 3.2D
  val BUS_COST = 1.8D

  def apply[F[_] : Monad](findZonesService: FindZonesService[F]): CalculatorFareService[F] =
    new CalculatorFareService[F](findZonesService)
}
