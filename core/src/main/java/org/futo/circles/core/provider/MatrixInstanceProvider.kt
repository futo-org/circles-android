package org.futo.circles.core.provider

import org.matrix.android.sdk.api.Matrix


object MatrixInstanceProvider {
    lateinit var matrix: Matrix
        private set

    fun saveMatrixInstance(matrixInstance: Matrix) {
        matrix = matrixInstance
    }
}