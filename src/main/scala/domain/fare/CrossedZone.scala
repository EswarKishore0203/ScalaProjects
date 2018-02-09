package domain.fare

sealed trait CrossedZone extends Product with Serializable {
  def crossed: Boolean

  def charge: Double
}

object CrossedZone {
  val MAX_COST = 3.2
  val BUS_COST = 1.8

  /**
    * List fare an according to zones
    */
  val zonesWithCharges = (minZonesCrossed: Int, zoneOneCrossed: Boolean) => {
    List(CrossedOnlyZoneOne(minZonesCrossed, zoneOneCrossed),
      CrossedOneZoneOutsideOfZoneOne(minZonesCrossed, zoneOneCrossed),
      CrossedTwoZonesIncludingZoneOne(minZonesCrossed, zoneOneCrossed),
      CrossedTwoZonesExcludingZoneOne(minZonesCrossed, zoneOneCrossed),
      CrossedThreeZones(minZonesCrossed),
      ByDefaultAllCrossed
    ).find(_.crossed)
  }

  def getCost(minZonesCrossed: Int, zoneOneCrossed: Boolean): Option[CrossedZone] =
    zonesWithCharges(minZonesCrossed, zoneOneCrossed)
}

import domain.fare.CrossedZone._

case class CrossedThreeZones(minZonesCrossed: Int) extends CrossedZone {
  override def crossed: Boolean = minZonesCrossed == 3

  override def charge: Double = MAX_COST
}

case class CrossedTwoZonesExcludingZoneOne(minZonesCrossed: Int, zoneOneCrossed: Boolean) extends CrossedZone {
  override def crossed: Boolean = minZonesCrossed == 2 && !zoneOneCrossed

  override def charge: Double = 2.25
}

case class CrossedTwoZonesIncludingZoneOne(minZonesCrossed: Int, zoneOneCrossed: Boolean) extends CrossedZone {
  override def crossed: Boolean = minZonesCrossed == 2 && zoneOneCrossed

  override def charge: Double = 3.0
}

case class CrossedOnlyZoneOne(minZonesCrossed: Int, zoneOneCrossed: Boolean) extends CrossedZone {
  override def crossed: Boolean = minZonesCrossed == 1 && zoneOneCrossed

  override def charge: Double = 2.5
}

case class CrossedOneZoneOutsideOfZoneOne(minZonesCrossed: Int, zoneOneCrossed: Boolean) extends CrossedZone {
  override def crossed: Boolean = minZonesCrossed == 1 && !zoneOneCrossed

  override def charge: Double = 2.0
}

case object ByDefaultAllCrossed extends CrossedZone {
  override def crossed: Boolean = true

  override def charge: Double = MAX_COST
}
