/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uib.probe.quantuploader.enums;

/**
 *
 * @author carlos.horro
 * Enum representing current supported Excel columns
 */
public enum Column {
    PUBMED_ID("PumedID"),STUDY_KEY("Study key"),AUTHOR("Author"),YEAR("Year"),QUANT_PROTEINS_NUM("# Quantified proteins"),ACCESSION_PROT("acc nu. uniprot/Nextprot"),
    PROTEIN_NAME_UNIPROT("Protein name from uniprot"), ACCESSION_PUB("acc number from publication"), PROTEIN_NAME_PUB("Protein name from publication"),
    PROTEIN_PEPTIDE("Protein or Peptide"),RAW_DATA("Raw data available"),PEPTIDES_ID_NUMBER("# Id peptides"),QUANT_PEPTIDES_NUMBER("# quantified peptide(s)"),
    PEPTIDE_CHARGE("Peptide charge"), PEPTIDE_SEQUENCE("Quant peptide seq"), PEPTIDE_SEQUENCE_ANNOTATED("Sequence annotated"), PEPTIDE_MODS("peptide modification/PTM"),
    PEPTIDE_COMMENT_MOD("Comment modification"), STUDY_TYPE("Type of study"), SAMPLE_TYPE("Sample type"), 
    PG1_NUMBER("n Patients group 1"), PG1("Patient group 1"), PSG1("Patient subgroup 1"), PG1_COMMENT("Comment Patient gr. 1"), 
    PG2_NUMBER("n Patients group 2"), PG2("Patient group 2"), PSG2("Patient subgroup 2"), PG2_COMMENT("Comment Patient gr. 2"),
    SAMPLE_MATCHING("Sample matching (volume or concentration)"), NORM_STRATEGY("Normalization strategy"), 
    FC_PG1_PG2("FC/Ratio Patient group 1 / Patient group 2"), LOG2FC_PG1_PG2("Log 2 FC/Ratio Patient group 1 / Patient group 2"),
    PVALUE("P value"), SIGNIFICANCE_TH("Significance threshold"), PVALUE_COMMENT("P value comment"), ROC_AUC("ROC AUC"), TECHNOLOGY("Technology"),
    ANALYTHICAL_METHOD("Analytical method"), ANALYTHICAL_APPROACH("Analytical approach"), QUANT_TYPE("Shotgun/targeted quant"), ENZYME("Enzyme"),
    QUANT_BASIS("Quantification basis"), QUANT_BASIS_COMMENT("Comment quant basis"), ADDITIONAL_COMMENTS("Additional comments"), DISEASE_GROUP_CAT("Disease group category"),
    POOLED_SAMPLES("Pooled samples")
    ;

    private final String columnName;

    private Column(String columnName) {
        this.columnName = columnName;
    }
    public String getColumnName() {
        return this.columnName;
    }
}

    
