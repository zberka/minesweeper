package minesweeper

import kotlin.random.Random
import kotlin.system.exitProcess

const val FIELD_SIZE = 9

enum class Action(val cmd: String)  {
    MARK("mine"),    //Set mines mark (or unmark)
    EXPLORE("free") //explore if free cell
}


/*
. as unexplored cells
/ as explored free cells without mines around it
Numbers from 1 to 8 as explored free cells with 1 to 8 mines around them, respectively
X as mines
* as unexplored marked cells
*/

enum class E (val symbol: String) {
    MINE("X"),
    EMPTY("."),
    FREE("/"),
    MARK("*"),
    NONE("")
}

class MineField(val fieldSize: Int, var minesCount: Int) {

    var boardGenerated = false
    var markedMinesCount: Int = 0
    var exploredCount: Int = 0
    val grid2D: Array<Array<String>> = Array(fieldSize) { Array(fieldSize) { E.EMPTY.symbol } }
    val grid2D_Marked: Array<Array<String>> = Array(fieldSize) { Array(fieldSize) { E.NONE.symbol } }
    val grid2D_Explored: Array<Array<String>> = Array(fieldSize) { Array(fieldSize) { E.NONE.symbol } }

    init {
        // Game LIMIT
        minesCount =  minesCount.coerceIn(1,fieldSize * fieldSize - 9)
    }

    fun genetateBoard(xNoMine : Int, yNoMine: Int) {

        boardGenerated = true
        //-----------------
        val emptyList = E.EMPTY.symbol.repeat((fieldSize * fieldSize - (minesCount)).coerceIn(0,fieldSize * fieldSize))
        val mineList = E.MINE.symbol.repeat((minesCount).coerceIn(0,fieldSize * fieldSize))
        // Concatenate and convert to a list of characters
        val combinedList = (emptyList + mineList).toList()
        // Shuffle the list to distribute mines randomly
        val shuffledList = combinedList.shuffled(Random(System.nanoTime()))
        // println(shuffledList)

        // create 2D array only .,X
        //--------------------------
        for (index in shuffledList.indices) {
          val value = shuffledList[index]
            grid2D[index  / fieldSize][index % fieldSize] = ""+value
        }
        //--------------------------

        // move 3x3 mines for GAME start
        for(x in xNoMine - 1 .. xNoMine +1 )
            for(y in yNoMine - 1 .. yNoMine +1 )
                moveOneMine(y-1, x-1)



        // Add Number of mines on '.' empty place(from 1 to 8)
        //-------------------------------------------------
        minesCount = 0
        for (row in 0 until fieldSize) {
            for (col in 0 until fieldSize) {

                //recalculate mines - after move
                if (grid2D[row][col] == E.MINE.symbol) minesCount++

                if (grid2D[row][col] == E.EMPTY.symbol) {
                    // count number of mines around
                    var aroundMines = 0
                    var rowStart = (row-1).coerceIn(0, fieldSize-1)
                    var rowEnd = (row+1).coerceIn(0, fieldSize-1)
                    var colStart = (col-1).coerceIn(0, fieldSize-1)
                    var coldEnd = (col+1).coerceIn(0, fieldSize-1)
                    for (rowCheck in rowStart..rowEnd)
                        for (colCheck in colStart.. coldEnd)
                            if (grid2D[rowCheck][colCheck] == E.MINE.symbol) aroundMines++

                    if (aroundMines != 0)  {
                        grid2D[row][col] = ("" + aroundMines)
                    }
                }

            }
        }

        //printFieldTable()  // just DEBUG
    }

    private fun moveOneMine(yNoMine: Int, xNoMine: Int) {
        if (yNoMine !in 0 until fieldSize || xNoMine !in 0 until fieldSize) return

        if (grid2D[yNoMine][xNoMine] == E.MINE.symbol) {
            outerLoop@ for (row in 0 until fieldSize)
                for (col in 0 until fieldSize)
                    if (grid2D[row][col] == E.EMPTY.symbol && row != yNoMine && col != xNoMine) {
                        grid2D[row][col] = E.MINE.symbol
                        grid2D[yNoMine][xNoMine] = E.EMPTY.symbol
                        break@outerLoop
                    }
        }
    }

    fun printFieldTable(printMines : Boolean = true) {
        print(" |")
        for (x in 0 until fieldSize) print(x+1)
        println("|")
        print("—|")
        for (x in 0 until fieldSize) print("—")
        println("|")
        // Print the array
        for (row in 0 until fieldSize) {
            print("${row+1}|")
            for (col in 0 until fieldSize) {
                if (!printMines && grid2D[row][col] == E.MINE.symbol)
                    print(E.EMPTY.symbol )
                else
                    print(grid2D[row][col])
            }
            println("|") // New line at the end of each row
        }
        print("—|")
        for (x in 0 until fieldSize) print("—")
        println("|")
        //println("fieldSize:$fieldSize minesCount:$minesCount")
    }

    // mark / unmark mine
    // and check WIN STATUS
    fun markMine(x: Int, y: Int) {

        if (grid2D_Marked[y - 1][x - 1] == E.NONE.symbol) {
            grid2D_Marked[y - 1][x - 1] = E.MARK.symbol
        } else {
            grid2D_Marked[y - 1][x - 1] = E.NONE.symbol
        }

        // Calculate marked mines
        for (row in 0 until fieldSize)
            for (col in 0 until fieldSize)
                if (grid2D[row][col] == E.MINE.symbol
                    && grid2D_Marked[row][col] == E.MARK.symbol)
                    markedMinesCount++


        // check WIN STATUS
        if (markedMinesCount == minesCount && boardGenerated) {
            printTableForUser()
            println("Congratulations! You found all the mines!")
            exitProcess(0)
        }

    }

    // explore - free field
    // and check LOSE STATUS
    fun exploreField(x: Int, y: Int) {

        if (x !in 1 ..fieldSize || y !in 1 .. fieldSize) return

        //First  free command cannot be a mine; it should always be empty.
        // You can achieve this in many ways – it's up to you.
        if (exploredCount == 0)   genetateBoard(x,y)

        // check if not explored
        if (grid2D_Explored[y - 1][x - 1] == E.NONE.symbol) {

            grid2D_Explored[y - 1][x - 1] = E.FREE.symbol
            exploredCount++
            //println("x: $x , y: $y '${grid2D[y - 1][x - 1]}' exploredCount:  $exploredCount" )

            // check LOSE STATUS mark is with MINE
            if (grid2D[y - 1][x - 1] == E.MINE.symbol)  {
                printTableForUser()
                println("You stepped on a mine and failed!")
                exitProcess(0)
            }

            // check WIN STATUS - explore all empty fields
            if (fieldSize * fieldSize - exploredCount == minesCount) {
                printTableForUser()
                println("Congratulations! You found all the mines!")
                exitProcess(0)
            }

            if (grid2D[y - 1][x - 1] !in "1" .. "8" )
            // move 3x3 mines for GAME start
                for(x2 in x - 1 .. x +1 )
                    for(y2 in y - 1 .. y +1 ) {
                        if (x2 in 1 ..fieldSize && y2 in 1 .. fieldSize
                            && grid2D_Explored[y2-1][x2-1] == E.NONE.symbol
                            && grid2D[y2 - 1][x2 - 1] != E.MINE.symbol) {
                            //println("x2: $x2 , y2: $y2 '${grid2D[y2 - 1][x2 - 1]}'" )
                            exploreField(x2, y2)
                        }
                    }


        }

        // FIX for mark setup before board generated
        // The last grid contains '*' and '/' characters that are next to each other.
        // This situation is impossible. If there is '*' character that is next to '/' it should be replaced to '/' or to a number.
        for (row in 0 until fieldSize) {
            for (col in 0 until fieldSize) {

                //recalculate mines - after move
                if (grid2D_Marked[row][col] == E.MARK.symbol && grid2D_Explored[row][col] == E.FREE.symbol)
                    if (   grid2D[row][col] != E.MINE.symbol
                        ) {
                        markedMinesCount--
                        grid2D_Marked[row][col] = E.NONE.symbol
                    }
            }
        }
    }


    fun printTableForUser() {
        print(" |")
        for (x in 0 until fieldSize) print(x+1)
        println("|")
        print("—|")
        for (x in 0 until fieldSize) print("—")
        println("|")
        // Print the array
        for (row in 0 until fieldSize) {
            print("${row+1}|")
            for (col in 0 until fieldSize) {

                // if marked field
                if (grid2D_Marked[row][col] == E.MARK.symbol) print(E.MARK.symbol)
                // if explored
                else if (grid2D_Explored[row][col] == E.FREE.symbol && grid2D[row][col] == E.EMPTY.symbol)
                    print(E.FREE.symbol)
                // print number
                else if (grid2D_Explored[row][col] == E.FREE.symbol && grid2D[row][col] in "1" .. "8")
                    print(grid2D[row][col])
                else if (grid2D[row][col] == E.EMPTY.symbol )print(grid2D[row][col])
                else print(E.EMPTY.symbol) //hidden mine or empty fieds

            }
            println("|") // New line at the end of each row
        }
        print("—|")
        for (x in 0 until fieldSize) print("—")
        println("|")

        //println("fieldSize:$fieldSize minesCount:$minesCount")
    }

}

fun main() {

    /*
    var mineTest  =  MineField(FIELD_SIZE, 10)
    mineTest.genetateBoard(2,8)
    mineTest.printFieldTable()
    mineTest.printTableForUser()
    exitProcess(0)
     */


    println("How many mines do you want on the field?")
    var minesNbr = readln().toInt().coerceIn(0, FIELD_SIZE * FIELD_SIZE)
    var mineField  =  MineField(FIELD_SIZE, minesNbr)


    mineField.printTableForUser()

    while(true) {
        println("Set/unset mines marks or claim a cell as free:")
        val (x, y, action) = readln().trim().split(" ")

        when (action) {
            Action.EXPLORE.cmd -> {
                mineField.exploreField(x.toInt(), y.toInt())
            }
            Action.MARK.cmd -> {
                mineField.markMine(x.toInt(), y.toInt())
            }
            else -> {
                println("HELP: x y [mine|free]")
            }

        }

        //mineField.printFieldTable()  // just DEBUG
        mineField.printTableForUser()
    }

}