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

package de.ukon.liger.webservice;


import de.ukon.liger.claimanalysis.ClaimAnalysis;
import de.ukon.liger.claimanalysis.ClaimComparisonReport;
import de.ukon.liger.main.DbaMain;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.logging.Logger;

@CrossOrigin
@RestController
@RequestMapping("/claim_analysis")
public class minimalPairController {

    public ClaimAnalysis ca;

    public minimalPairController(ClaimAnalysis ca){
        this.ca = ca;
    }

    private final static Logger LOGGER = Logger.getLogger(DbaMain.class.getName());

    /*
    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/apply_rule", produces = "application/json", consumes = "application/json")
    public TestGraph applyRuleRequest(@RequestBody AnnotationRequest request) {

    }

     */

    /***
     * This function checks whether a specific classifier has been applied to a sentence.
     *
     * @param cr A claim request contains an input sentence, an output sentence, and a classifier
     * @return boolean (success) and an explanation (if unsuccessful) stored in a ClaimComparisonReport object.
     */

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @RequestMapping(value = "/compare_claims",consumes = "application/json")
    public ClaimComparisonReport compareClaims(@RequestBody ClaimRequest cr) {

        return ca.compareClaimRequest(cr);
    }

    /***
     * For an input spring, specifies the set of classifiers that apply to this tring.
     * @param
     * @return Set of classifiers that apply to the given claim.
     */

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @RequestMapping(value = "/claim_analysis", produces = "application/json")
    public Set<String> produceClaimAnalysis(
    @RequestParam(value = "in", defaultValue = "Didn't pass sentence") String input) {





        return null;
    }


}
