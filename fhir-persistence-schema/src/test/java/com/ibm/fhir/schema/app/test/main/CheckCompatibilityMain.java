/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.schema.app.test.main;

import com.ibm.fhir.schema.app.Main;
import com.ibm.fhir.schema.app.test.main.helper.TestHelper;

public class CheckCompatibilityMain {

    public static void main(String[] args) {
        String[] arguments = {
                "--prop-file", TestHelper.absolutePathToProperties(),
                "--pool-size", "2",
                "--schema-name", "FHIRDATA",
                "--check-compatibility"
        };
        Main.main(arguments);
    }
}