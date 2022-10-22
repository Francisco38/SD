package com.example.demo;

import java.util.*;

import com.example.data.Team;
import com.example.data.Player;
import com.example.data.Users;
import com.example.data.Event;
import com.example.data.Game;
import com.example.data.TeamStats;
import com.example.demo.services.PlayerService;
import com.example.demo.services.TeamService;
import com.example.demo.services.UserService;
import com.example.demo.services.GameService;
import com.example.demo.services.EventService;
import com.example.data.Login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import javax.servlet.http.HttpSession;

@Controller
public class DataController {
    @Autowired
    TeamService teamService;

    @Autowired
    PlayerService playerService;

    @Autowired
    UserService userService;

    @Autowired
    GameService gameService;

    @Autowired
    EventService eventService;

    @GetMapping("/")
    public String home() {
        return "redirect:/menu";
    }

    @GetMapping("/menu")
    public String menu(Model m) {
        int c = checkPermissions();
        if (c == 0) {
            return "menu/menu_unlogged";
        } else if (c == 1) {
            return "menu/menu";
        } else {
            return "menu/menu_admin";
        }
    }

    @GetMapping("/addAdmin")
    public String addAdmin(Model m) {
        this.userService.addUser(new Users("Francisco", "fariafrancisco85@gmail.com", "123", 925194219, 2));
        return "redirect:/menu";
    }

    @GetMapping("/update")
    public String update() throws IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();

        // Teams
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://v3.football.api-sports.io/teams?league=94&season=2021"))
                .setHeader("x-rapidapi-hosh", "v3.football.api-sports.io")
                .setHeader("x-rapidapi-key", "435d06d9edb33f32e1771d9980a635e0").build();

        HttpResponse<String> responseRaw = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONArray response = new JSONObject(responseRaw.body()).getJSONArray("response");

        ArrayList<Team> teams = new ArrayList<Team>();
        for (int i = 0; i < 5; i++) {
            JSONObject teamInfo = response.getJSONObject(i).getJSONObject("team");
            String id = teamInfo.getString("id");
            String name = teamInfo.getString("name");
            String code = teamInfo.getString("code");
            String country = teamInfo.getString("country");
            String founded = teamInfo.getString("founded");
            String logo = teamInfo.getString("logo");

            if (id != "null" && name != "null" && code != "null" && country != "null" && founded != "null"
                    && logo != "null") {
                Team team = new Team(Integer.parseInt(id), name, code, country, Integer.parseInt(founded), logo);
                teams.add(team);
            }
        }

        for (Team team : teams) {
            this.teamService.addTeam(team);
        }

        for (int i = 0; i < teams.size(); i++) {

            // Players
            request = HttpRequest.newBuilder()
                    .uri(URI.create("https://v3.football.api-sports.io/players/squads?team=" + teams.get(i).getId()))
                    .setHeader("x-rapidapi-hosh", "v3.football.api-sports.io")
                    .setHeader("x-rapidapi-key", "435d06d9edb33f32e1771d9980a635e0").build();

            responseRaw = client.send(request, HttpResponse.BodyHandlers.ofString());

            String error = new JSONObject(responseRaw.body()).getString("results");
            if (error.equals("0")) {
                System.out.println("ERROR");
                break;
            }

            response = new JSONObject(responseRaw.body()).getJSONArray("response");
            JSONArray playersInfo = response.getJSONObject(0).getJSONArray("players");

            for (int j = 0; j < playersInfo.length(); j++) {
                JSONObject playerInfo = playersInfo.getJSONObject(j);
                String id = playerInfo.getString("id");
                String name = playerInfo.getString("name");
                String age = playerInfo.getString("age");
                String number = playerInfo.getString("number");
                String position = playerInfo.getString("position");
                String photo = playerInfo.getString("photo");

                if (id != "null" && name != "null" && age != "null" && number != "null" &&
                        position != "null"
                        && photo != "null") {
                    Player player = new Player(Integer.parseInt(id), teams.get(i).getId(), name,
                            Integer.parseInt(age),
                            Integer.parseInt(number), position, photo);
                    this.playerService.addPlayer(player);
                    teams.get(i).addPlayers(player);
                }
            }
            this.teamService.updateTeam(teams.get(i));

        }

        return "redirect:/listTeams";
    }

    @GetMapping("/logOut")
    public String logOut() {
        setPermissions(0);
        return "redirect:/menu";
    }

    @GetMapping("/login")
    public String login(Model m) {
        m.addAttribute("user", new Users());
        return "login/login";
    }

    @PostMapping("/login")
    public String loginP(@ModelAttribute Login login) {
        Users u = this.userService.getUser(login.getUsername());
        if (u == null)
            return "login/login_failed";
        if (u.getPassword().equals(login.getPassword())) {
            setPermissions(u.getPermission());
            return "redirect:/menu";
        } else
            return "login/login_failed";

    }

    @GetMapping("/register")
    public String register(Model m) {
        m.addAttribute("user", new Users());
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute Users s) {
        this.userService.addUser(s);
        return "redirect:/listUser";
    }

    @GetMapping("/listUser")
    public String listUser(Model model) {
        if (checkPermissions() != 2) {
            return "redirect:/menu";
        }
        model.addAttribute("users", this.userService.getAllUsers());
        return "user/listUser";
    }

    @GetMapping("/createPlayer")
    public String createPlayer(@RequestParam(name = "id", required = true) int id, Model m) {
        if (checkPermissions() != 2) {
            return "redirect:/menu";
        }
        m.addAttribute("player", new Player(id));
        return "player/createPlayer";
    }

    @GetMapping("/editPlayer")
    public String editPlayer(@RequestParam(name = "id", required = true) int id, Model m) {
        if (checkPermissions() != 2) {
            return "redirect:/menu";
        }
        Optional<Player> p = this.playerService.getPlayer(id);
        if (p.isPresent()) {
            m.addAttribute("player", p.get());
            return "player/editPlayer";
        } else {
            return "redirect:/listPlayers";
        }
    }

    @PostMapping("/addPlayers")
    public String addPlayer(@ModelAttribute Player player) {
        Optional<Team> op = this.teamService.getTeam(player.getTeam_id());
        if (op.isPresent()) {
            this.playerService.addPlayer(player);
            op.get().addPlayers(player);
            this.teamService.updateTeam(op.get());
            return "redirect:/team?id=" + player.getTeam_id();
        }
        return "redirect:/listTeams";
    }

    @PostMapping("/savePlayers")
    public String savePlayer(@ModelAttribute Player player) {
        this.playerService.addPlayer(player);
        return "redirect:/team?id=" + player.getTeam_id();
    }

    @GetMapping("/listPlayers")
    public String listPlayers(Model model) {
        model.addAttribute("players", this.playerService.getAllPlayers());
        int c = checkPermissions();
        if (c == 2) {
            return "player/listPlayersAdmin";
        } else if (c == 1) {
            return "player/listPlayers";
        } else {
            return "player/listPlayers_unlogged";
        }
    }

    @GetMapping("/listTeams")
    public String listTeams(Model model) {
        model.addAttribute("teams", this.teamService.getAllTeams());
        int c = checkPermissions();
        if (c == 2) {
            return "team/listTeams_admin";
        } else if (c == 1) {
            return "team/listTeams";
        } else {
            return "team/listTeams_unlogged";
        }
    }

    @GetMapping("/team")
    public String team(@RequestParam(name = "id", required = true) int id, Model m) {
        Optional<Team> team = this.teamService.getTeam(id);
        if (team.isPresent()) {
            m.addAttribute("team", team.get());
            int c = checkPermissions();
            if (c == 2) {
                return "team/teamAdmin";
            } else if (c == 1) {
                return "team/team";
            } else {
                return "team/team_unlogged";
            }
        } else {
            return "redirect:/listTeams";
        }
    }

    @GetMapping("/createTeam")
    public String createTeam(Model m) {
        if (checkPermissions() != 2) {
            return "redirect:/menu";
        }
        m.addAttribute("team", new Team());
        return "team/editTeam";
    }

    @GetMapping("/editTeam")
    public String editTeam(@RequestParam(name = "id", required = true) int id, Model m) {
        if (checkPermissions() != 2) {
            return "redirect:/menu";
        }
        Optional<Team> op = this.teamService.getTeam(id);
        if (op.isPresent()) {
            m.addAttribute("team", op.get());
            return "team/editTeam";
        }
        return "redirect:/listTeams";
    }

    @PostMapping("/saveTeam")
    public String saveTeam(@ModelAttribute Team team) {
        this.teamService.addTeam(team);
        return "redirect:/listTeams";
    }

    @GetMapping("/createGame")
    public String createGame(Model m) {
        if (checkPermissions() != 2) {
            return "redirect:/menu";
        }
        m.addAttribute("game", new Game());
        m.addAttribute("allTeams", this.teamService.getAllTeams());
        return "game/createGame";
    }

    @PostMapping("/saveGame")
    public String saveGame(@ModelAttribute Game game) {
        Team t1 = this.teamService.getTeam(game.getTeam1Id()).get();
        Team t2 = this.teamService.getTeam(game.getTeam2Id()).get();
        game.setTeam1(t1);
        game.setTeam2(t2);
        this.gameService.addGame(game);
        return "redirect:/listGames";
    }

    @GetMapping("/listGames")
    public String listGames(Model model) {
        List<Game> games = this.gameService.getAllGames();
        model.addAttribute("games", games);
        int c = checkPermissions();
        if (c == 2) {
            return "game/listGames_admin";
        } else if (c == 1) {
            return "game/listGames";
        } else {
            return "game/listGames_unlogged";
        }
    }

    public String myFunc(Event e) {
        return e.getData();
    }

    @GetMapping("/gameDetails")
    public String gameDetails(@RequestParam(name = "id", required = true) int id,
            Model m) {
        Optional<Game> g = this.gameService.getGame(id);
        if (g.isPresent()) {
            Collections.sort(g.get().getEvents());
            m.addAttribute("game", g.get());
            int c = checkPermissions();
            if (c == 2) {
                return "game/game_admin";
            } else if (c == 1) {
                return "game/game";
            } else {
                return "game/game_unlogged";
            }
        }
        return "redirect:/listGames";
    }

    @GetMapping("/addEvent")
    public String addEvent(@RequestParam(name = "id", required = true) int id, Model m) {
        if (checkPermissions() < 1) {
            return "redirect:/menu";
        }
        Optional<Game> g = this.gameService.getGame(id);
        if (g.isPresent()) {
            m.addAttribute("game", g.get());
            return "event/addEvent";
        } else {
            return "redirect:/listGames";
        }
    }

    @GetMapping("/createEvent")
    public String createEvent(@RequestParam(name = "id", required = true) int id,
            @RequestParam(name = "type", required = true) int type, Model m) {
        if (checkPermissions() < 1) {
            return "redirect:/menu";
        }
        m.addAttribute("event", new Event(id, type));
        if (type == 0 || type == 1 || type == 5 || type == 6) {
            return "event/event1";
        } else if (type == 2 || type == 3 || type == 4) {
            Optional<Game> g = this.gameService.getGame(id);
            m.addAttribute("game", g.get());
            return "event/event2";
        } else {
            return "redirect:/menu";
        }
    }

    @PostMapping("/saveEvent")
    public String saveEvent(@ModelAttribute Event event) {
        int type = event.getType();
        Optional<Game> op = this.gameService.getGame(event.getGame_id());
        if (type == 0) {
            event.setMessage("Start");
            op.get().addEvents_Check(event);
            eventService.addEvent(event);
            gameService.addGame(op.get());
            return "redirect:/listGames";
        } else if (type == 1) {
            event.setMessage("End");
            op.get().addEvents_Check(event);
            eventService.addEvent(event);
            gameService.addGame(op.get());
            return "redirect:/listGames";
        } else if (type == 2) {
            Optional<Player> p = this.playerService.getPlayer(event.getPlayer_id());
            Optional<Team> t = this.teamService.getTeam(p.get().getTeam_id());
            event.setMessage("Goal by " + p.get().getName() + " from " + t.get().getName());
            op.get().addEvents_Check(event);
            eventService.addEvent(event);
            gameService.addGame(op.get());
            return "redirect:/listGames";
        } else if (type == 3) {
            Optional<Player> p = this.playerService.getPlayer(event.getPlayer_id());
            Optional<Team> t = this.teamService.getTeam(p.get().getTeam_id());
            event.setMessage("Yellow card for " + p.get().getName() + " from " + t.get().getName());
            op.get().addEvents_Check(event);
            eventService.addEvent(event);
            gameService.addGame(op.get());
            return "redirect:/listGames";
        } else if (type == 4) {
            Optional<Player> p = this.playerService.getPlayer(event.getPlayer_id());
            Optional<Team> t = this.teamService.getTeam(p.get().getTeam_id());
            event.setMessage("Red card for " + p.get().getName() + " from " + t.get().getName());
            op.get().addEvents_Check(event);
            eventService.addEvent(event);
            gameService.addGame(op.get());
            return "redirect:/listGames";
        } else if (type == 5) {
            event.setMessage("Interrupted");
            op.get().addEvents_Check(event);
            eventService.addEvent(event);
            gameService.addGame(op.get());
            return "redirect:/listGames";
        } else if (type == 6) {
            event.setMessage("Resumed");
            op.get().addEvents_Check(event);
            eventService.addEvent(event);
            gameService.addGame(op.get());
            return "redirect:/listGames";
        } else {
            return "redirect:/listGames";
        }
    }

    @GetMapping("/checkEvents")
    public String checkEvents(@RequestParam(name = "id", required = true) int id, Model m) {
        if (checkPermissions() < 2) {
            return "redirect:/menu";
        }
        Optional<Game> g = this.gameService.getGame(id);
        if (g.isPresent()) {
            m.addAttribute("game", g.get());
            return "event/checkEvents";
        } else {
            return "redirect:/listGames";
        }
    }

    @GetMapping("/acceptEvent")
    public String acceptEvent(@RequestParam(name = "id", required = true) int id, Model m) {
        if (checkPermissions() < 2) {
            return "redirect:/menu";
        }
        Optional<Event> e = this.eventService.getEvent(id);
        int game_id = e.get().getGame_id();
        Optional<Game> g = this.gameService.getGame(game_id);
        Optional<Player> p = this.playerService.getPlayer(e.get().getPlayer_id());
        if (e.isPresent()) {
            g.get().deleteEvents_Check(e.get());
            g.get().addEvents(e.get());
            if (e.get().getType() == 2) {
                if (p.get().getTeam_id() == g.get().getTeam1Id()) {
                    int s = g.get().getScore1() + 1;
                    g.get().setScore1(s);
                } else {
                    int s = g.get().getScore2() + 1;
                    g.get().setScore2(s);
                }
            }
            this.gameService.addGame(g.get());
            return "redirect:/checkEvents?id=" + e.get().getGame_id();
        } else {
            return "redirect:/listGames";
        }

    }

    @GetMapping("/denyEvent")
    public String denyEvent(@RequestParam(name = "id", required = true) int id, Model m) {
        if (checkPermissions() < 2) {
            return "redirect:/menu";
        }
        Optional<Event> e = this.eventService.getEvent(id);
        Optional<Game> g = this.gameService.getGame(e.get().getGame_id());
        if (e.isPresent()) {
            g.get().deleteEvents_Check(e.get());
            eventService.deleteEvent(e.get());
            this.gameService.addGame(g.get());
            return "redirect:/checkEvents?id=" + g.get().getId();
        } else {
            return "redirect:/listGames";
        }
    }

    @GetMapping("/printStats")
    public String printStats(@RequestParam(name = "old_d", required = true) int old_d,
            @RequestParam(name = "d", required = true) int d, Model m) {
        List<String> g = this.gameService.getTeamStats(d);
        List<TeamStats> l = new ArrayList<>();
        if (old_d == d && old_d < 10) {
            for (int i = g.size() - 1; i >= 0; i--) {
                String[] a = g.get(i).split(",");
                l.add(new TeamStats(a[0], a[1], a[2], a[3], a[4], a[5], a[6]));
            }
            m.addAttribute("v", 10 + d);
        } else {
            for (int i = 0; i < g.size(); i++) {
                String[] a = g.get(i).split(",");
                l.add(new TeamStats(a[0], a[1], a[2], a[3], a[4], a[5], a[6]));
            }
            m.addAttribute("v", d);
        }

        m.addAttribute("team", l);

        String player = this.playerService.getBestPlayer();
        if (player == null) {
            return "printStats_noPlayer";
        }
        String[] temp = player.split(",");
        Optional<Player> p = this.playerService.getPlayer(Integer.parseInt(temp[0]));

        m.addAttribute("player", p.get());
        m.addAttribute("goals", temp[1]);

        return "printStats";
    }

    @GetMapping("/compTeams")
    public String compTeams(Model m) {
        m.addAttribute("allTeams", this.teamService.getAllTeams());
        m.addAttribute("values", new TeamStats());
        return "getTeams";
    }

    @PostMapping("/compTeams")
    public String postcompTeams(@ModelAttribute TeamStats t, Model m) {
        int a = Integer.parseInt(t.getTeam1());
        int b = Integer.parseInt(t.getTeam2());
        String g = this.gameService.getTeamComp(a, b);
        String s = this.gameService.getCardNumbers(a, b);
        String[] t1 = g.split(",");
        String[] t2 = s.split(",");

        m.addAttribute("team1", this.teamService.getTeam(a).get());
        m.addAttribute("team2", this.teamService.getTeam(b).get());
        m.addAttribute("info", new TeamStats(t1[0], t1[1], t1[2], t1[3], t1[4], t1[5], t2[0], t2[1], t2[2], t2[3]));
        m.addAttribute("allTeams", this.teamService.getAllTeams());
        m.addAttribute("values", new TeamStats());
        return "getTeamsInfo";
    }

    public int checkPermissions() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession(true);
        Integer counter = (Integer) session.getAttribute("counter");
        int c;
        if (counter == null) {
            c = 0;
            session.setAttribute("counter", c);
        } else {
            c = counter;
        }
        return c;
    }

    public void setPermissions(int i) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession(true);
        session.setAttribute("counter", i);
    }

}