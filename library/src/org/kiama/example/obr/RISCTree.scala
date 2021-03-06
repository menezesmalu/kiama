/*
 * This file is part of Kiama.
 *
 * Copyright (C) 2009-2015 Anthony M Sloane, Macquarie University.
 * Copyright (C) 2010-2015 Dominic Verity, Macquarie University.
 *
 * Kiama is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Kiama is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Kiama.  (See files COPYING and COPYING.LESSER.)  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package org.kiama
package example.obr

/**
 * Module containing structures for representing RISC machine programs.
 * In essence this is a register-less assembly code, in which the flow of
 * data between constructs is represented in the parent-child relationships
 * of a tree structure.
 */
object RISCTree {

    import org.kiama.relation.Tree

    /**
     * Tree type for RISC programs.
     */
    type RISCTree = Tree[RISCNode,RISCProg]

    /**
     * Superclass of all RISC tree nodes.
     */
    sealed abstract class RISCNode extends Product

    /**
     * Trait to mark those nodes which need to have a return value
     * allocated to a register.
     */
    trait NeedsRegister extends RISCNode

    /**
     * A stack program consisting of the given statements.
     */
    case class RISCProg (insns : List[Item]) extends RISCNode

    /**
     * Superclass of all item constructs, ie. target constructs that do
     * not return a value to their enclosing construct.
     */
    sealed abstract class Item extends RISCNode

    /**
     * Branch to the given label if the value of the given datum is
     * equal to zero.
     */
    case class Beq (cond : Datum, dest : Label) extends Item

    /**
     * Branch to the given label if the value of the given datum is
     * not equal to zero.
     */
    case class Bne (cond : Datum, dest : Label) extends Item

    /**
     * Jump unconditionally to the given label.
     */
    case class Jmp (dest : Label) extends Item

    /**
     * Define the given at this point in the item sequence.
     */
    case class LabelDef (lab : Label) extends Item

    /**
     * Cause the program to terminate.
     */
    case class Ret () extends Item

    /**
     * Evaluate d and store its value in the memory location given by mem.
     */
    case class StW (mem : Address, d : Datum) extends Item

    /**
     * Calculate and write out the value of the given datum to standard
     * output.
     */
    case class Write (d : Datum) extends Item

    /**
     * Superclass of all datum constructs, ie target constructs that
     * return a value to their enclosing construct.  reg is the local
     * register (%l1 to %l7) that is being used to store the value of
     * this datum (set by the encoder).
     */
    sealed abstract class Datum extends RISCNode with NeedsRegister

    /**
     * Read an integer value from standard input.
     */
    case class Read () extends Datum

    /**
     * Add two word values and return the sum l + r.
     */
    case class AddW (l : Datum, r : Datum) extends Datum

    /**
     * Evaluate cond and if the value is true (non-zero) return the value
     * of t, else return the value of f.
     */
    case class Cond (cond : Datum, t : Datum, f : Datum) extends Datum

    /**
     * Compare two word values for equality and return 1 if they are
     * equal and 0 if they are not.
     */
    case class CmpeqW (l : Datum, r : Datum) extends Datum

    /**
     * Compare two word values for inequality and return 1 if they are
     * equal and 0 if they are not.
     */
    case class CmpneW (l : Datum, r : Datum) extends Datum

    /**
     * Compare two word values and return 1 if the left operand is
     * greater then the right operands, otherwise return 0.
     */
    case class CmpgtW (l : Datum, r : Datum) extends Datum

    /**
     * Compare two word values and return 1 if the left operand is
     * less then the right operands, otherwise return 0.
     */
    case class CmpltW (l : Datum, r : Datum) extends Datum

    /**
     * Divide two word values and return the dividend from l / r.
     */
    case class DivW (l : Datum, r : Datum) extends Datum

    /**
     * Return the given integer value.
     */
    case class IntDatum (num : Int) extends Datum

    /**
     * Load a value from memory address given by mem.
     */
    case class LdW (mem : Address) extends Datum

    /**
     * Multiple two word values and return l * r.
     */
    case class MulW (l : Datum, r : Datum) extends Datum

    /**
     * Negate a word value and return the negation of e.
     */
    case class NegW (d : Datum) extends Datum

    /**
     * Complement a Boolean value.
     */
    case class Not (d : Datum) extends Datum

    /**
     * Divide two word values and return the remainder from l / r.
     */
    case class RemW (l : Datum, r : Datum) extends Datum

    /**
     * Subtract two word values and return the difference l - r.
     */
    case class SubW (l : Datum, r : Datum) extends Datum

    /**
     * A compound datum, consisting of a sequence of items to execute
     * followed by a datum whose value is evaluated and returned.
     */
    case class SequenceDatum (insns : List[Item], d : Datum) extends Datum

    /**
     * Memory addresses that can be stored to or loaded from.
     */
    sealed abstract class Address extends RISCNode

    /**
     * An address that is calculated by an known integer offset from
     * the start of the local memory block.
     */
    case class Local (offset : Int) extends Address

    /**
     * An address that is calculated by a indexing from the local address
     * addr.  In this case the offset is calculated by a datum.
     */
    case class Indexed (base : Local, offset : Datum) extends Address with NeedsRegister

    /**
     * Labels represent a code position.
     */
    case class Label (num : Int)

}
