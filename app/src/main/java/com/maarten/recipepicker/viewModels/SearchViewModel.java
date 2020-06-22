package com.maarten.recipepicker.viewModels;

import androidx.lifecycle.ViewModel;

import java.util.Set;
import java.util.TreeSet;

public class SearchViewModel extends ViewModel {
    private Set<String> selectedCategories = new TreeSet<>();

    public Set<String> getSelectedCategories() {
        return selectedCategories;
    }

    public void setSelectedCategories(Set<String> selectedCategories) {
        this.selectedCategories = selectedCategories;
    }
}
