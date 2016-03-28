package lineage

import breeze.linalg._
import org.scalatest.FunSuite
import pipelines.Logging
import utils.ImageMetadata

class CollapseMappingSuite extends FunSuite with Logging {
  test("CollapseMapping Vector2Int Test"){
    val v = DenseVector.zeros[Double](4)
    val i = 3
    val mapping = CollapseMapping(v, i)
    assert(mapping.qForward(List(Coor(0))).toString == "List(0)")
    assert(mapping.qBackward(List(Coor(0))).toString == "List(0, 1, 2, 3)")
  }

  test("CollapseMapping Matrix2Vector Test"){
    val m = DenseMatrix.zeros[Double](5, 4)
    val v = DenseVector.zeros[Double](4)
    val mapping1 = CollapseMapping(m, v, 0)
    assert(mapping1.qForward(List(Coor(0,0), Coor(0,1), Coor(1,1))).toString == "List(0, 1)")
    assert(mapping1.qBackward(List(Coor(0))).toString == "List((0,0), (1,0), (2,0), (3,0), (4,0))")

    val v2 = DenseVector.zeros[Double](5)
    val mapping2 = CollapseMapping(m, v, 1)
    assert(mapping2.qForward(List(Coor(0,0), Coor(0,1), Coor(1,1))).toString == "List(0, 1)")
    assert(mapping2.qBackward(List(Coor(0))).toString == "List((0,0), (0,1), (0,2), (0,3))")
  }

  test("CollapseMapping Image2Matrix Test"){
    val image = ImageMetadata(5,4,3)
    val m1 = DenseMatrix.zeros[Double](5,4)
    val mapping1 = CollapseMapping(image, m1, 2)
    assert(mapping1.qForward(List(Coor(0,0,0))).toString == "List((0,0))")
    assert(mapping1.qBackward(List(Coor(0,0))).toString == "List((0,0,0), (0,0,1), (0,0,2))")

    val m2 = DenseMatrix.zeros[Double](5,3)
    val mapping2 = CollapseMapping(image, m2, 1)
    assert(mapping2.qForward(List(Coor(0,0,0))).toString == "List((0,0))")
    assert(mapping2.qBackward(List(Coor(0,0))).toString == "List((0,0,0), (0,1,0), (0,2,0), (0,3,0))")

    val m3 = DenseMatrix.zeros[Double](4,3)
    val mapping3 = CollapseMapping(image, m3, 0)
    assert(mapping3.qForward(List(Coor(0,0,0))).toString == "List((0,0))")
    assert(mapping3.qBackward(List(Coor(0,0))).toString == "List((0,0,0), (1,0,0), (2,0,0), (3,0,0), (4,0,0))")
  }

  test("CollapseMapping Int2Vector Test"){
    val v = DenseVector.zeros[Double](4)
    val i = 3
    val mapping = CollapseMapping(i, v)
    assert(mapping.qForward(List(Coor(0))).toString == "List(0, 1, 2, 3)")
    assert(mapping.qBackward(List(Coor(0))).toString == "List(0)")
  }
  
  test("CollapseMapping Vector2Matrix Test"){
    val m = DenseMatrix.zeros[Double](5, 4)
    val v = DenseVector.zeros[Double](4)
    val mapping1 = CollapseMapping(v, m, 0)
    assert(mapping1.qForward(List(Coor(0))).toString == "List((0,0), (1,0), (2,0), (3,0), (4,0))")
    assert(mapping1.qBackward(List(Coor(0,0), Coor(0,1))).toString == "List(0, 1)")

    val v2 = DenseVector.zeros[Double](5)
    val mapping2 = CollapseMapping(v, m, 1)
    assert(mapping2.qForward(List(Coor(0))).toString == "List((0,0), (0,1), (0,2), (0,3))")
    assert(mapping2.qBackward(List(Coor(0,0), Coor(0,1))).toString == "List(0)")
  }

  test("CollapseMapping Matrix2Image Test"){
    val image = ImageMetadata(5,4,3)
    val m1 = DenseMatrix.zeros[Double](5,4)
    val mapping1 = CollapseMapping(m1, image, 2)
    assert(mapping1.qForward(List(Coor(0,0))).toString == "List((0,0,0), (0,0,1), (0,0,2))")
    assert(mapping1.qBackward(List(Coor(0,1,2))).toString == "List((0,1))")
    

    val m2 = DenseMatrix.zeros[Double](5,3)
    val mapping2 = CollapseMapping(m2, image, 1)
    assert(mapping2.qForward(List(Coor(0,0))).toString == "List((0,0,0), (0,1,0), (0,2,0), (0,3,0))")
    assert(mapping2.qBackward(List(Coor(0,1,2))).toString == "List((0,2))")


    val m3 = DenseMatrix.zeros[Double](4,3)
    val mapping3 = CollapseMapping(m3, image, 0)
    assert(mapping3.qForward(List(Coor(0,0))).toString == "List((0,0,0), (1,0,0), (2,0,0), (3,0,0), (4,0,0))")
    assert(mapping3.qBackward(List(Coor(0,1,2))).toString == "List((1,2))")
  }

  test("CollapseMapping Vector2Singular Query Optimization Test"){
    val v = DenseVector.zeros[Double](100000)
    val i = 3
    
    val fKeys = (for(i <- 0 until v.size) yield Coor(i)).toList
    val bKeys = List(Coor(0))

    val trials = 3
    val list1 = (0 until trials).map(x => {
      val mapping = CollapseMapping(v, i)
      assert(mapping.qForward(fKeys) == List(Coor(0)))
      time(mapping.qForward(fKeys))
    }).toList

    val list2 = (0 until trials).map(x => {
      val mapping = CollapseMapping(v, i)
      assert(mapping.qBackward(bKeys).size == 100000)
      time(mapping.qBackward(bKeys))
    }).toList
    
    Mapping.setOpzFlag(true)
    
    val list3 = (0 until trials).map(x => {
      val mapping = CollapseMapping(v, i)
      assert(mapping.qForward(fKeys) == List(Coor(0)))
      time(mapping.qForward(fKeys))
    }).toList

    val list4 = (0 until trials).map(x => {
      val mapping = CollapseMapping(v, i)
      assert(mapping.qBackward(bKeys).size == 100000)
      time(mapping.qBackward(bKeys))
    }).toList

    println("non-optimized forward: "+list1.sum/list1.size)
    println("optimized forward: "+list3.sum/list3.size)
    println("non-optimized backward: "+list2.sum/list2.size)
    println("optimized backward: "+list4.sum/list4.size)
    Mapping.setOpzFlag(false)
  }

  test("CollapseMapping Matrix2Vector Query Optimization Test"){
    val m = DenseMatrix.zeros[Double](10, 100000)
    val v = DenseVector.zeros[Double](10)
    
    val fKeys = (for(i <- 0 until m.rows-1; j <- 0 until m.cols) yield Coor(i, j)).toList
    val bKeys = (for(i <- 0 until v.size-1) yield Coor(i)).toList

    val trials = 3
    val list1 = (0 until trials).map(x => {
      val mapping = CollapseMapping(m, v, 1)
      assert(mapping.qForward(fKeys).size == 9)
      time(mapping.qForward(fKeys))
    }).toList
    
    val list2 = (0 until trials).map(x => {
      val mapping = CollapseMapping(m, v, 1)
      assert(mapping.qBackward(bKeys).size == 900000)
      time(mapping.qBackward(bKeys))
    }).toList

    Mapping.setOpzFlag(true)

    val list3 = (0 until trials).map(x => {
      val mapping = CollapseMapping(m, v, 1)
      assert(mapping.qForward(fKeys).size == 9)
      time(mapping.qForward(fKeys))
    }).toList

    val list4 = (0 until trials).map(x => {
      val mapping = CollapseMapping(m, v, 1)
      assert(mapping.qBackward(bKeys).size == 900000)
      time(mapping.qBackward(bKeys))
    }).toList

    println("non-optimized forward: "+list1.sum/list1.size)
    println("optimized forward: "+list3.sum/list3.size)
    println("non-optimized backward: "+list2.sum/list2.size)
    println("optimized backward: "+list4.sum/list4.size)
    Mapping.setOpzFlag(false)
  }


  def time[A](f: => A) = {
    val s = System.nanoTime
    val ret = f
    (System.nanoTime-s)/1e9
  }
}