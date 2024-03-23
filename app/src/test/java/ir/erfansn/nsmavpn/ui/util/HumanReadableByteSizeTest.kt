package ir.erfansn.nsmavpn.ui.util

import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.log
import kotlin.random.Random

class HumanReadableByteSizeTest {

    @Test(expected = IllegalArgumentException::class)
    fun throwsException_whenPassedNumberIsNegative() {
        val byteSize = -1L

        byteSize.toHumanReadableByteSizeAndUnit()
    }

    @Test
    fun returnsSizeWithBytesUnit_whenPassedNumberIsBelowThan1024() {
        val byteSize = Random.nextLong(0, 1024)

        val (size, unit) = byteSize.toHumanReadableByteSizeAndUnit(fakeUnitNames)

        assertThat(size.toLong()).isEqualTo(byteSize)
        assertThat(unit).isEqualTo(fakeUnitNames.first())
    }

    @Test
    fun returnsSizeWithNUnit_whenPassedNumberIsAboveThan1024() {
        val byteSize = Random.nextLong(1024, Long.MAX_VALUE)

        val (size, unit) = byteSize.toHumanReadableByteSizeAndUnit(fakeUnitNames)

        assertThat(size.toDouble()).isIn(Range.closedOpen(0.0, 1024.0))
        assertThat(unit).isEqualTo(fakeUnitNames[log(byteSize.toDouble(), 1024.0).toInt()])
    }
}

private val fakeUnitNames = Array(7) {
    it.toString()
}
