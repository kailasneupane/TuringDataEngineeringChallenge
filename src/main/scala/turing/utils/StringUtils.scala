package turing.utils


object StringUtils {

  def getMaxDepthOfParenthesis(str: String): Int = {
    var currentMax = 0
    var max = 0
    for (i <- 0 until str.length) {
      val currentChar = str.charAt(i)
      if (currentChar == '(') {
        currentMax += 1
        if (currentMax > max)
          max = currentMax
      } else if (currentChar == ')') {
        if (currentMax > 0)
          currentMax -= 1
      } else {
        -1
      }
    }
    if (currentMax != 0) {
      -1
    } else {
      max
    }
  }


  def getPositionOfClosingParenthesis(str: String, startPosition: Int = 0): Int = {
    var counter = 0
    var mathedPosition = -1
    var found = false
    var n = startPosition
    while (n < str.length && !found) {
      if (str.charAt(n) == '(') {
        counter += 1
      } else if (str.charAt(n) == ')') {
        counter -= 1
        if (counter == 0) {
          mathedPosition = n
          found = true
        }
      }
      n += 1
    }
    mathedPosition
  }


  def separateOuterParenthesisByComma(str: String): String = {
    var parStrTemp = str
    var finalStr = ""
    var firstParenthesisLastIndex = getPositionOfClosingParenthesis(str, 0) + 1
    finalStr += parStrTemp.substring(0, firstParenthesisLastIndex)
    parStrTemp = parStrTemp.substring(firstParenthesisLastIndex)
    while (parStrTemp.length > 0) {
      firstParenthesisLastIndex = getPositionOfClosingParenthesis(parStrTemp, 0) + 1
      finalStr += "," + parStrTemp.substring(0, firstParenthesisLastIndex)
      parStrTemp = parStrTemp.substring(firstParenthesisLastIndex)
    }
    finalStr
  }


  def getNestingFactor(parenthesisedStr: String): Double = {
    var nestValue = 0
    var wholeParenthesisStrSeparatedByComma = separateOuterParenthesisByComma(parenthesisedStr)
    var outerParenthesisChunk: Array[String] = wholeParenthesisStrSeparatedByComma.split(",")
    for (i <- 0 until outerParenthesisChunk.length) {
      nestValue += getMaxDepthOfParenthesis(outerParenthesisChunk(i))
    }
    1.0 * nestValue / outerParenthesisChunk.length
  }


  def main(args: Array[String]): Unit = {
    var str = "( ((X)) (((Y))) )()()()"
    println(getMaxDepthOfParenthesis(str))
    println(getPositionOfClosingParenthesis(str))
    println(separateOuterParenthesisByComma(str))
  }

}
