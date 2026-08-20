package com.nagy_mark.mygamevault.models;

import java.util.List;

public class Game {
    private String name;
    private Cover cover;
    private Long first_release_date;
    private List<InvolvedCompany> involved_companies;

    public String getName() {
        return name;
    }

    public Cover getCover() {
        return cover;
    }

    public Long getFirstReleaseDate() {
        return first_release_date;
    }

    public String getPublisherName() {
        if (involved_companies != null) {
            for (InvolvedCompany ic : involved_companies) {
                if (ic.isPublisher() && ic.getCompany() != null) {
                    return ic.getCompany().getName();
                }
            }
        }
        return null;
    }

    public static class Cover {
        private String image_id;
        public String getImageId() {
            return image_id;
        }
    }

    public static class InvolvedCompany {
        private boolean publisher;
        private Company company;

        public boolean isPublisher() { return publisher; }
        public Company getCompany() { return company; }
    }

    public static class Company {
        private String name;
        public String getName() { return name; }
    }
}
