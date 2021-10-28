/*
 * "
 *     Copyright (C) 2021 Mark-Matthias Zymla
 *
 *     This file is part of the abstract syntax annotator  (https://github.com/Mmaz1988/abstract-syntax-annotator-web/blob/master/README.md).
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * "
 */

package de.ukon.liger.webservice.rest;


import de.ukon.liger.claimanalysis.ClaimAnalysis;
import de.ukon.liger.claimanalysis.ClaimComparisonReport;
import de.ukon.liger.claimanalysis.Classifier;
import de.ukon.liger.webservice.rest.dtos.ClaimRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@CrossOrigin
@RestController
@RequestMapping("/claim_analysis")
public class MinimalPairController {

    public ClaimAnalysis ca;

    public MinimalPairController(ClaimAnalysis ca) {
        this.ca = ca;
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(MinimalPairController.class);

    /***
     * This function checks whether a specific classifier has been applied to a sentence.
     *
     * @param cr A claim request contains an input sentence, an output sentence, and a classifier
     * @return boolean (success) and an explanation (if unsuccessful) stored in a ClaimComparisonReport object.
     */

    @CrossOrigin
    @RequestMapping(value = "/compare_claims", consumes = "application/json")
    public ClaimComparisonReport compareClaims(@RequestBody ClaimRequest cr) {
        return ca.compareClaimRequest(cr);
    }

    /***
     * For an input spring, specifies the set of classifiers that apply to this tring.
     * @param
     * @return Set of classifiers that apply to the given claim.
     */
    @CrossOrigin
    @RequestMapping(value = "/claim_analysis", produces = "application/json")
    public Set<Classifier> produceClaimAnalysis(@RequestParam(value = "in", defaultValue = "Didn't pass sentence") String input) {
        return ca.getNonPresentClassifiers(input);
    }


}
