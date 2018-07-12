package fpbookclub.knightsquest

/**
  * @author caleb
  */
case class KnightPos(c: Int, r: Int) {
  def possibleMoves: List[KnightPos] =
    List(
      KnightPos(c + 2, r + 1),
      KnightPos(c + 1, r + 2),
      KnightPos(c + 2, r - 1),
      KnightPos(c + 1, r - 2),
      KnightPos(c - 2, r + 1),
      KnightPos(c - 1, r + 2),
      KnightPos(c - 2, r - 1),
      KnightPos(c - 1, r - 2)
    ).filter(pos => pos.c >= 1 && pos.c <= 8 && pos.r >= 1 && pos.r <= 8)

  def reachableIn3: List[KnightPos] = {
    val firstPositions  = possibleMoves
    val secondPositions = firstPositions.flatMap(pos => pos.possibleMoves)
    val thirdPositions  = secondPositions.flatMap(pos => pos.possibleMoves)
    thirdPositions
  }

  def canReachIn3(pos: KnightPos): Boolean = reachableIn3.contains(pos)
}
