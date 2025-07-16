package com.titanaxis.model;

public class CategoryTrend {
    private final String categoryName;
    private final double percentageChange;

    public CategoryTrend(String categoryName, double percentageChange) {
        this.categoryName = categoryName;
        this.percentageChange = percentageChange;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getPercentageChange() {
        return percentageChange;
    }
}