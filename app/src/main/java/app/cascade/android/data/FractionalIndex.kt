package app.cascade.android.data

object FractionalIndex {
    private const val DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    private const val ZERO = '0'
    private const val LAST = 'z'
    private val SMALLEST_INTEGER = "A" + ZERO.toString().repeat(26)

    fun between(a: String?, b: String?): String {
        a?.let(::validate)
        b?.let(::validate)
        require(a == null || b == null || a < b) { "$a >= $b" }
        if (a == null) {
            if (b == null) return "a$ZERO"
            val ib = integerPart(b)
            val fb = b.substring(ib.length)
            if (ib == SMALLEST_INTEGER) return ib + midpoint("", fb)
            if (ib < b) return ib
            return requireNotNull(decrement(ib)) { "cannot decrement any more" }
        }
        val ia = integerPart(a)
        val fa = a.substring(ia.length)
        if (b == null) {
            return increment(ia) ?: (ia + midpoint(fa, null))
        }
        val ib = integerPart(b)
        val fb = b.substring(ib.length)
        if (ia == ib) return ia + midpoint(fa, fb)
        val i = requireNotNull(increment(ia)) { "cannot increment any more" }
        return if (i < b) i else ia + midpoint(fa, null)
    }

    private fun midpoint(a: String, b: String?): String {
        require(b == null || a < b) { "$a >= $b" }
        require(!a.endsWith(ZERO) && b?.endsWith(ZERO) != true) { "trailing zero" }
        if (b != null) {
            var n = 0
            while (n < b.length && (a.getOrNull(n) ?: ZERO) == b[n]) n++
            if (n > 0) return b.substring(0, n) + midpoint(a.drop(n), b.substring(n))
        }
        val digitA = if (a.isNotEmpty()) DIGITS.indexOf(a[0]) else 0
        val digitB = if (b != null) DIGITS.indexOf(b[0]) else DIGITS.length
        if (digitB - digitA > 1) {
            return DIGITS[Math.round(0.5 * (digitA + digitB)).toInt()].toString()
        }
        if (b != null && b.length > 1) return b.substring(0, 1)
        return DIGITS[digitA] + midpoint(a.drop(1), null)
    }

    private fun integerLength(head: Char): Int = when (head) {
        in 'a'..'z' -> head - 'a' + 2
        in 'A'..'Z' -> 'Z' - head + 2
        else -> throw IllegalArgumentException("invalid order key head: $head")
    }

    private fun integerPart(key: String): String {
        val length = integerLength(key[0])
        require(length <= key.length) { "invalid order key: $key" }
        return key.substring(0, length)
    }

    private fun validate(key: String) {
        require(key != SMALLEST_INTEGER) { "invalid order key: $key" }
        val fraction = key.substring(integerPart(key).length)
        require(!fraction.endsWith(ZERO)) { "invalid order key: $key" }
    }

    private fun increment(x: String): String? {
        val head = x[0]
        val digits = x.substring(1).toCharArray()
        var carry = true
        var i = digits.size - 1
        while (carry && i >= 0) {
            val d = DIGITS.indexOf(digits[i]) + 1
            if (d == DIGITS.length) {
                digits[i] = ZERO
            } else {
                digits[i] = DIGITS[d]
                carry = false
            }
            i--
        }
        if (!carry) return head + String(digits)
        if (head == 'Z') return "a$ZERO"
        if (head == 'z') return null
        val next = head + 1
        val rest = String(digits)
        return next + if (next > 'a') rest + ZERO else rest.dropLast(1)
    }

    private fun decrement(x: String): String? {
        val head = x[0]
        val digits = x.substring(1).toCharArray()
        var borrow = true
        var i = digits.size - 1
        while (borrow && i >= 0) {
            val d = DIGITS.indexOf(digits[i]) - 1
            if (d == -1) {
                digits[i] = LAST
            } else {
                digits[i] = DIGITS[d]
                borrow = false
            }
            i--
        }
        if (!borrow) return head + String(digits)
        if (head == 'a') return "Z$LAST"
        if (head == 'A') return null
        val previous = head - 1
        val rest = String(digits)
        return previous + if (previous < 'Z') rest + LAST else rest.dropLast(1)
    }
}
