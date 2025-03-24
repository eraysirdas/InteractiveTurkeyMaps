package com.eraysirdas.turkeymaps.model;

public class SearchDataByTypeRequest {

    public String IsBalType;
    public String SearchQuery;
    public String SearchType;

    public SearchDataByTypeRequest(String isBalType, String searchQuery, String searchType) {
        IsBalType = isBalType;
        SearchQuery = searchQuery;
        SearchType = searchType;
    }
}
