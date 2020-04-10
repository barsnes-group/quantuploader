/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uib.probe.quantuploader.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author carlos.horro
 * Enum to help manage different representation of diseases found in different papers
 */
public enum DiseaseCategory {
    ALZHEIMER("Alzheimer's",new HashSet<>(Arrays.asList(new String[]{"AD"/*,"DE"*/}))), // TODO: IS "DE" ALSO A VALID INPUT FOR ALZHEIMER??
    PARKINSON("Parkinson's",new HashSet<>(Arrays.asList(new String[]{"PD","Parkinson"}))),
    ALS("Amyotrophic Lateral Sclerosis",new HashSet<>(Arrays.asList(new String[]{"ALS"}))),
    MS("Multiple Sclerosis",new HashSet<>(Arrays.asList(new String[]{"MS"})))
    ;
    
    private final String description;
    private final Set possibleInputs;

    private DiseaseCategory(String description, Set possibleInputs) {
        this.description = description;
        this.possibleInputs = possibleInputs;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public Set getPossibleInputs() {
        return this.possibleInputs;
    }
    
    public static DiseaseCategory getDiseaseGroupByInput(String input) {
        if (input == null)
            return null;
        input = input.trim();
        if (ALZHEIMER.getPossibleInputs().contains(input))
            return ALZHEIMER;
        else if (PARKINSON.getPossibleInputs().contains(input))
            return PARKINSON;
        else if (ALS.getPossibleInputs().contains(input))
            return ALS;
        else if (MS.getPossibleInputs().contains(input))
            return MS;
        else return null;        
    }
    
}
