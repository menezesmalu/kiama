/*
 * This file is part of Kiama.
 *
 * Copyright (C) 2009-2015 Anthony M Sloane, Macquarie University.
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
package example.lambda2

/**
 * Interface to switchable evaluators for the lambda2 language.  Enables
 * a client to select an evaluation mechanism by name and access the
 * evaluator for that mechanism.
 */
object Evaluators {

   /**
     * Map of evaluator names to the evaluators themselves.
     * Comments refer to names in Dolstra and Visser paper.
     */
    val evalmap =
        Map ("reduce"         -> new ReduceEvaluator,          // eval1
             "reducesubst"    -> new ReduceSubstEvaluator,     // eval2
             "innermostsubst" -> new InnermostSubstEvaluator,  // eval3
             "eagersubst"     -> new EagerSubstEvaluator,      // eval4, eval5
             "lazysubst"      -> new LazySubstEvaluator,       // eval6
             "pareagersubst"  -> new ParEagerSubstEvaluator,   // eval7
             "parlazysubst"   -> new ParLazySubstEvaluator,    // eval8
             "parlazyshare"   -> new ParLazyShareEvaluator,    // eval9
             "parlazyupdate"  -> new ParLazyUpdateEvaluator)   // eval10

    /**
     * Return the names of the available evaluation mechanisms.
     */
    val mechanisms = evalmap.keySet

    /**
     * The evaluator for the given mechanism.
     */
    def evaluatorFor (mechanism : String) : Evaluator =
        evalmap (mechanism)

}
