package domain.zone

class FindZonesService[F[_]](zoneRepositoryAlgebra: ZoneRepositoryAlgebra[F]) {
  def haveYouCrossedZoneOne(from: List[Int],
                            to: List[Int],
                            minZonesCrossed: Int): Boolean =
    (from.lengthCompare(1) == 0 && from.contains(1)) || (to.lengthCompare(1) == 0 && to.contains(1))


  def findMinimumNumberOfZoneCrossed(from: List[Int], to: List[Int]): Int = {
    var minZonesVisited: Int = Int.MaxValue
    for (fromZone <- from) {
      for (toZone <- to) {
        val zonesVisited: Int = Math.abs(fromZone - toZone) + 1
        if (zonesVisited < minZonesVisited) minZonesVisited = zonesVisited
      }
    }
    minZonesVisited
  }

  def getZonesByCode(stationCode: String): F[List[Int]] =
    zoneRepositoryAlgebra.getZonesByCode(stationCode)

}

object FindZonesService {
  def apply[F[_]](zoneRepositoryAlgebra: ZoneRepositoryAlgebra[F]): FindZonesService[F] =
    new FindZonesService(zoneRepositoryAlgebra)
}
