package com.portal.enums;

import com.portal.model.Classifier;

public enum DateType {

    CREATE_DATE(new Classifier(170, "CREATE_DATE", "DATE_TYPE")),
    VALIDITY_DATE(new Classifier(171, "VALIDITY_DATE", "DATE_TYPE"));

    private Classifier type;

    DateType(Classifier type) {
        this.type = type;
    }

    public Classifier getType() {
        return type;
    }

    public static DateType getById(Integer id) {
        if (id != null) {
            for (DateType type : DateType.values()) {
                if (type.getType().getId().equals(id)) {
                    return type;
                }
            }
        }

        throw new IllegalArgumentException("DateType - Não foi encontrado o ENUM com o ID " + id);
    }

    public static DateType getByValue(String value) {
        if (value != null) {
            for (DateType type : DateType.values()) {
                if (type.getType().getValue().equals(value)) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("DateType - Não foi encontrado o ENUM com o value " + value);
    }
}
