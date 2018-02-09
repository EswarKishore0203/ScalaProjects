package domain.barrier


sealed trait Type extends Product with Serializable

object Type {

  case object Bus extends Type

  case object Tube extends Type

}


object Direction extends Enumeration {
  type Direction = Value
  val IN, OUT = Value
}

/**
  * This class represent barrier information like where he/she tapped in/out card and charge at barrier
  * user will be charge maximum fare when he/she passes through the inward barrier at the station
  * @param code: String
  * @param `type`: Type type of travel
  * @param direction: Duration type of direction like IN/OUT
  * @param charge: Double charge at station
  */
case class Barrier(code: String,
                   `type`: Type,
                   direction: Direction.Value,
                   charge: Double)
