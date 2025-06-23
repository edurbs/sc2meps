package com.github.edurbs.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ScriptureEarthBookName {

    BOOK_GEN(MepsBookName.BOOK_01_GEN),
    BOOK_EXO(MepsBookName.BOOK_02_EXO),
    // BOOK_LEV(MepsBookName.BOOK_03_LEV),
    // BOOK_NUM(MepsBookName.BOOK_04_NUM),
    // BOOK_DEU(MepsBookName.BOOK_05_DEU),
    BOOK_JOS(MepsBookName.BOOK_06_JOS),
    BOOK_JDG(MepsBookName.BOOK_07_JUD),
    BOOK_RUT(MepsBookName.BOOK_08_RUT),
    BOOK_1SA(MepsBookName.BOOK_09_1SA),
    BOOK_2SA(MepsBookName.BOOK_10_2SA),
    // BOOK_1KI(MepsBookName.BOOK_11_1KI),
    // BOOK_2KI(MepsBookName.BOOK_12_2KI),
    // BOOK_1CH(MepsBookName.BOOK_13_1CH),
    // BOOK_2CH(MepsBookName.BOOK_14_2CH),
    BOOK_EZR(MepsBookName.BOOK_15_EZR),
    BOOK_NEH(MepsBookName.BOOK_16_NEH),
    BOOK_EST(MepsBookName.BOOK_17_EST),
    // BOOK_JOB(MepsBookName.BOOK_18_JOB),
    BOOK_PSA(MepsBookName.BOOK_19_PSA),
    BOOK_PRO(MepsBookName.BOOK_20_PRO),
    // BOOK_ECC(MepsBookName.BOOK_21_ECC),
    // BOOK_SNG(MepsBookName.BOOK_22_SON),
    // BOOK_ISA(MepsBookName.BOOK_23_ISA),
    // BOOK_JER(MepsBookName.BOOK_24_JER),
    // BOOK_LAM(MepsBookName.BOOK_25_LAM),
    // BOOK_EZK(MepsBookName.BOOK_26_EZE),
    BOOK_DAN(MepsBookName.BOOK_27_DAN),
    // BOOK_HOS(MepsBookName.BOOK_28_HOS),
    BOOK_JOL(MepsBookName.BOOK_29_JOE),
    BOOK_AMO(MepsBookName.BOOK_30_AMO),
    // BOOK_OBA(MepsBookName.BOOK_31_OBA),
    BOOK_JON(MepsBookName.BOOK_32_JON),
    // BOOK_MIC(MepsBookName.BOOK_33_MIC),
    // BOOK_NAM(MepsBookName.BOOK_34_NAH),
    // BOOK_HAB(MepsBookName.BOOK_35_HAB),
    // BOOK_ZEP(MepsBookName.BOOK_36_ZEP),
    // BOOK_HAG(MepsBookName.BOOK_37_HAG),
    // BOOK_ZEC(MepsBookName.BOOK_38_ZEC),
    // BOOK_MAL(MepsBookName.BOOK_39_MAL),
    BOOK_MAT(MepsBookName.BOOK_40_MAT),
    BOOK_MRK(MepsBookName.BOOK_41_MAR),
    BOOK_LUK(MepsBookName.BOOK_42_LUK),
    BOOK_JHN(MepsBookName.BOOK_43_JOH),
    BOOK_ACT(MepsBookName.BOOK_44_ACT),
    BOOK_ROM(MepsBookName.BOOK_45_ROM),
    BOOK_1CO(MepsBookName.BOOK_46_1CO),
    BOOK_2CO(MepsBookName.BOOK_47_2CO),
    BOOK_GAL(MepsBookName.BOOK_48_GAL),
    BOOK_EPH(MepsBookName.BOOK_49_EPH),
    BOOK_PHP(MepsBookName.BOOK_50_PHI),
    BOOK_COL(MepsBookName.BOOK_51_COL),
    BOOK_1TH(MepsBookName.BOOK_52_1TH),
    BOOK_2TH(MepsBookName.BOOK_53_2TH),
    BOOK_1TI(MepsBookName.BOOK_54_1TI),
    BOOK_2TI(MepsBookName.BOOK_55_2TI),
    BOOK_TIT(MepsBookName.BOOK_56_TIT),
    BOOK_PHM(MepsBookName.BOOK_57_PHM),
    BOOK_HEB(MepsBookName.BOOK_58_HEB),
    BOOK_JAS(MepsBookName.BOOK_59_JAM),
    BOOK_1PE(MepsBookName.BOOK_60_1PE),
    BOOK_2PE(MepsBookName.BOOK_61_2PE),
    BOOK_1JN(MepsBookName.BOOK_62_1JO),
    BOOK_2JN(MepsBookName.BOOK_63_2JO),
    BOOK_3JN(MepsBookName.BOOK_64_3JO),
    BOOK_JUD(MepsBookName.BOOK_65_JUD),
    BOOK_REV(MepsBookName.BOOK_66_REV);

    private final MepsBookName mepsBookName;

    public String getName() {
        return this.toString().substring(5);
    }

    public MepsBookName getMepsName() {
        return this.mepsBookName;
    }

}