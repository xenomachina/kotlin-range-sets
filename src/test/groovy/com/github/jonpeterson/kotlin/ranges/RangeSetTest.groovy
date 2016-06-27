package com.github.jonpeterson.kotlin.ranges

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class RangeSetTest extends Specification {

    private static kotlin.ranges.IntRange groovyToKotlinRange(IntRange range) {
        return new kotlin.ranges.IntRange(range.from, range.to)
    }

    private static List<kotlin.ranges.IntRange> groovyToKotlinRanges(List<IntRange> ranges) {
        return ranges.collect { range -> groovyToKotlinRange(range) }
    }


    def 'containsValue: #value'() {
        expect:
        def set = new IntRangeSet(groovyToKotlinRanges([3..5, 7..9, 13..16]))
        def emptySet = new IntRangeSet()
        set.containsValue(value) == contained
        emptySet.containsValue(value) == false

        where:
        value | contained
        0     | false
        1     | false
        2     | false
        3     | true
        4     | true
        5     | true
        6     | false
        7     | true
        8     | true
        9     | true
        10    | false
        11    | false
        12    | false
        13    | true
        14    | true
        15    | true
        16    | true
        17    | false
    }

    def 'containsAll: #contains'() {
        expect:
        def set = new IntRangeSet(groovyToKotlinRanges([3..5, 7..9, 13..16]))
        def emptySet = new IntRangeSet()
        def ranges = groovyToKotlinRanges(contains)
        set.containsAll(ranges) == contained
        emptySet.containsAll(ranges) == false

        where:
        contains             | contained
        [0..2]               | false
        [3..3]               | true
        [3..5]               | true
        [3..6]               | false
        [3..7]               | false
        [12..14]             | false
        [14..15]             | true
        [13..16]             | true
        [13..17]             | false
        [12..16]             | false
        [3..5, 8..8, 14..15] | true
        [3..6, 8..8, 14..15] | false
        [2..17]              | false
        [3..3, 8..8, 14..14] | true
    }

    def 'addAll: #adds'() {
        expect:
        def set = new IntRangeSet()
        set.addAll(groovyToKotlinRanges(adds)) == true
        set as List == groovyToKotlinRanges(expected)

        where:
        adds                                | expected
        [3..5, 3..5]                        | [3..5]
        [3..5, 1..5]                        | [1..5]
        [3..5, 3..7]                        | [3..7]
        [3..5, 1..4]                        | [1..5]
        [3..5, 4..7]                        | [3..7]
        [3..5, 2..7]                        | [2..7]
        [2..7, 3..5]                        | [2..7]
        [3..5, 7..9, 4..8]                  | [3..9]
        [3..5, 8..9, 13..16, 10..11, 6..6]  | [3..6, 8..11, 13..16]
        [3..5, 7..9, 13..16, 20..25, 6..21] | [3..25]
        [2..4, 0..1, 1..1, 0..0, 2..3]      | [0..4]
        [3..5, 1..2]                        | [1..5]
    }

    def 'removeAll: #removes'() {
        expect:
        def set = new IntRangeSet(groovyToKotlinRanges([3..5, 7..9, 13..16]))
        def emptySet = new IntRangeSet()
        def ranges = groovyToKotlinRanges(removes)
        set.removeAll(ranges) == removed
        emptySet.removeAll(ranges) == false
        set as List == groovyToKotlinRanges(expected)
        emptySet as List == []

        where:
        removes         | removed | expected
        [0..2]          | false   | [3..5, 7..9, 13..16]
        [6..6]          | false   | [3..5, 7..9, 13..16]
        [10..12]        | false   | [3..5, 7..9, 13..16]
        [1..3]          | true    | [4..5, 7..9, 13..16]
        [1..4]          | true    | [5..5, 7..9, 13..16]
        [1..5]          | true    | [7..9, 13..16]
        [1..6]          | true    | [7..9, 13..16]
        [1..7]          | true    | [8..9, 13..16]
        [5..7]          | true    | [3..4, 8..9, 13..16]
        [4..7]          | true    | [3..3, 8..9, 13..16]
        [3..7]          | true    | [8..9, 13..16]
        [14..15]        | true    | [3..5, 7..9, 13..13, 16..16]
        [0..2, 11..12]  | false   | [3..5, 7..9, 13..16]
        [2..7, 14..15]  | true    | [8..9, 13..13, 16..16]
        [2..10, 12..18] | true    | []
    }

    def 'retainAll: #retains'() {
        expect:
        def set = new IntRangeSet(groovyToKotlinRanges([3..7, 12..16, 22..27]))
        def emptySet = new IntRangeSet()
        def ranges = groovyToKotlinRanges(retains)
        set.retainAll(ranges) == changed
        emptySet.retainAll(ranges) == false
        set as List == groovyToKotlinRanges(expected)
        emptySet as List == []

        where:
        retains                 | changed | expected
        [3..7]                  | true    | [3..7]
        [2..7]                  | true    | [3..7]
        [3..8]                  | true    | [3..7]
        [2..8]                  | true    | [3..7]
        [3..27]                 | false   | [3..7, 12..16, 22..27]
        [2..28]                 | false   | [3..7, 12..16, 22..27]
        [4..26]                 | true    | [4..7, 12..16, 22..26]
        [14..23]                | true    | [14..16, 22..23]
        [3..7, 12..16, 22..27]  | false   | [3..7, 12..16, 22..27]
        [4..13, 15..24]         | true    | [4..7, 12..13, 15..16, 22..24]
        [2..15, 13..26, 22..28] | false   | [3..7, 12..16, 22..27]
        [4..15, 13..26, 22..26] | true    | [4..7, 12..16, 22..26]
    }

    @Unroll
    def 'hashCode/equals: #ranges'() {
        expect:
        def setA = new IntRangeSet(groovyToKotlinRanges([1..3, 7..10, 15..15]))
        def setB = new IntRangeSet(groovyToKotlinRanges(ranges))
        (setA.hashCode() == setB.hashCode()) == equal
        (setA == setB) == equal

        where:
        ranges                | equal
        [1..3, 7..10, 15..15] | true
        [2..3, 7..10, 15..15] | false
        [7..10, 1..3, 15..15] | true
    }
}
