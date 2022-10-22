package com.example.data;

public class TeamStats {
    private String id;
    private String numberOfGames;
    private String wins;
    private String def;
    private String emp;
    private String goals1;
    private String goals2;
    private String team1;
    private String team2;
    private String yellow1;
    private String red1;
    private String yellow2;
    private String red2;

    public TeamStats() {
    }

    public TeamStats(String id, String numberOfGames, String wins, String def, String emp, String goals1,
            String goals2) {
        this.id = id;
        this.numberOfGames = numberOfGames;
        this.wins = wins;
        this.def = def;
        this.emp = emp;
        this.goals1 = goals1;
        this.goals2 = goals2;
    }

    public TeamStats(String numberOfGames, String wins, String def, String emp, String goals1, String goals2,
            String yellow1, String red1, String yellow2, String red2) {
        this.numberOfGames = numberOfGames;
        this.wins = wins;
        this.def = def;
        this.emp = emp;
        this.goals1 = goals1;
        this.goals2 = goals2;
        this.yellow1 = yellow1;
        this.red1 = red1;
        this.yellow2 = yellow2;
        this.red2 = red2;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumberOfGames() {
        return numberOfGames;
    }

    public void setNumberOfGames(String numberOfGames) {
        this.numberOfGames = numberOfGames;
    }

    public String getWins() {
        return wins;
    }

    public void setWins(String wins) {
        this.wins = wins;
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public String getEmp() {
        return emp;
    }

    public void setEmp(String emp) {
        this.emp = emp;
    }

    public String getGoals1() {
        return goals1;
    }

    public void setGoals1(String goals1) {
        this.goals1 = goals1;
    }

    public String getGoals2() {
        return goals2;
    }

    public void setGoals2(String goals2) {
        this.goals2 = goals2;
    }

    public String getTeam1() {
        return team1;
    }

    public void setTeam1(String team1) {
        this.team1 = team1;
    }

    public String getTeam2() {
        return team2;
    }

    public void setTeam2(String team2) {
        this.team2 = team2;
    }

    public String getYellow1() {
        return yellow1;
    }

    public void setYellow1(String yellow1) {
        this.yellow1 = yellow1;
    }

    public String getRed1() {
        return red1;
    }

    public void setRed1(String red1) {
        this.red1 = red1;
    }

    public String getYellow2() {
        return yellow2;
    }

    public void setYellow2(String yellow2) {
        this.yellow2 = yellow2;
    }

    public String getRed2() {
        return red2;
    }

    public void setRed2(String red2) {
        this.red2 = red2;
    }

}
