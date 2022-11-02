package application.demo.controller;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import application.demo.service.UserService;
import org.aspectj.apache.bcel.classfile.Unknown;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

//import application.demo.exception.UserNotFoundException;
import application.demo.model.User;
import application.demo.repository.UserRepository;
import org.springframework.web.servlet.ModelAndView;

import javax.swing.text.html.parser.Entity;

@Controller
@RequestMapping("/users")
public class UserController {
	int numberPerPage = 10;
	Map<Integer, List<User>> userPerPage = new HashMap<>();

	List<User> users = new ArrayList<>();
	@Autowired
	private UserService userService;

	private List<User> importUser(List<User> userList) {
		if (userList.size() != 0) return userList;
		List<User> list = new ArrayList<>();
		User user;
		for(Integer i=0; i< 98; i++){
			user = new User( i, generateStringName(), 	"abn" + generateTwoChar(), "xuan@gmail.com", (int)(Math.random() * 100) +1);
			userService.save(user);
			list.add(user);
		}
		return list;
	}
	private int countPages(int numberPerPage){
		if(userService.findAllUsers().size() % numberPerPage != 0){
			return userService.findAllUsers().size() / numberPerPage +1;
		}
		return userService.findAllUsers().size() / numberPerPage;
	}
	private List<Integer> pageList(int pages){
		List<Integer> myList = IntStream.range(1, pages + 1).boxed()
				.collect(Collectors.toList());

		return myList;
	}
	public String generateStringName() {
		int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();

		String generatedString = random.ints(leftLimit, rightLimit + 1)
				.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
				.limit(targetStringLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();

		return generatedString;
	}

	public String generateTwoChar() {
		int leftLimit = 48; // numeral '0'
		int rightLimit = 56; // letter 'z'
		int targetStringLength = 2;
		Random random = new Random();

		String generatedString = random.ints(leftLimit, rightLimit + 1)
				.limit(targetStringLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();

		return generatedString;
	}

	public List<User> sortUserByOrder(List<User> users, String[] attributes, String direction) {
		Map<String, Function<User, String>> mapAttrs = new HashMap<>();
		mapAttrs.put("firstName", User::getFirstName);
		mapAttrs.put("lastName", User::getLastName);
		mapAttrs.put("email", User::getEmail);
		boolean firstRound = true;
		Comparator<User> compare = Comparator.comparing(mapAttrs.get(attributes[0]));
		for (String attr:attributes) {
			if (firstRound) {
				firstRound = false;
				continue;
			}
			compare = compare.thenComparing(mapAttrs.get(attr));
		}
		System.out.println("Sort direction: "+direction);
		if (direction.equals("DESC")) {
			compare = compare.reversed();
		}

		List<User> sorted = users.stream()
				.sorted(compare)
				.collect(Collectors.toList());
		return sorted;
	}

	public List<User> sortUserByOrder(List<User> users, String direction){
		List<User> tempList = new ArrayList<>();
		tempList = users;

		Comparator<User> compareByName = Comparator
				.comparing(User::getLastName)
				.thenComparing(User::getId);

		if (direction.equals("DESC")) {
			compareByName = compareByName.reversed();
			System.out.println("running");
		}

		List<User> sortedUsers = tempList.stream()
				.sorted(compareByName)
				.collect(Collectors.toList());
		return sortedUsers;
	}

	private Map<Integer, List<User>> genPagesData (String direction) {
		Map<Integer, List<User>> map = new HashMap<>();

		users = sortUserByOrder(importUser(users), new String[]{"firstName", "email"}, direction);

		users = sortUserByOrder(importUser(users), direction);
		int maxUsers = users.size();
		int maxPage = (int)Math.floor(maxUsers / numberPerPage) + 1;
		int extraUser = maxUsers % numberPerPage;

		if(maxUsers < numberPerPage){
			map.put(1, users.subList(0, extraUser));
		} else{
			for (int i = 0; i < maxPage - 1; i++) {
				int countTo = i * numberPerPage + numberPerPage;
				map.put(i+1, users.subList(i * numberPerPage, countTo));
				if (i == maxPage - 2) {
					map.put(i+2, users.subList(i * numberPerPage, i * numberPerPage + extraUser));
				}
			}
		}

		return map;
	}


	@GetMapping("register")
	public String employeeForm() {

		return "index";
	  }

	//add mapping for "/list"
	@GetMapping({"/list"})
	public String userList(@RequestParam(value="direction", required=false) String direction, Model theModel) {
		Map<String, String> sortState = new HashMap<>();
		sortState.put("ASC", "⇧");
		sortState.put("DESC", "⇩");

		System.out.println(direction);

		if (direction == null) direction = "ASC";
		System.out.println(direction);
		userPerPage = genPagesData(direction);

		theModel.addAttribute("previousPage", 0);
		theModel.addAttribute("nextPage", 2);
		theModel.addAttribute("users", userPerPage.get(1));
		theModel.addAttribute("sortType", direction.equals("ASC") ? "DESC" : "ASC");
		theModel.addAttribute("sortTypeSymbol", sortState.get(direction));
		theModel.addAttribute("pages", pageList(countPages(numberPerPage)));
		return "list-user";
	}

	@GetMapping({"/list/{page}"})
	public String userList(@PathVariable Integer page, Model theModel) {
		List<User> users = userService.findAllUsers();
		int maxUsers = users.size();
		int maxPage = (int)Math.floor(maxUsers / numberPerPage) + 1;

		int nextPage = page + 1;
		int previousPage = page - 1;
		theModel.addAttribute("nextMaxPage", maxPage +1);
		theModel.addAttribute("nextPage", nextPage);
		theModel.addAttribute("previousPage", previousPage);
		theModel.addAttribute("users", userPerPage.get(page));
		theModel.addAttribute("pages", pageList(countPages(numberPerPage)));
		return "list-user";
	}

	@PostMapping("/register")
	public String userRegistration(@ModelAttribute User user, Model model) {
		System.out.println(user.toString());
		System.out.println(user.getFirstName());
		System.out.println(user.getLastName());
		System.out.println(user.getEmail());

//		User user_inserted = userService.findUserById(user.getId());
		
//		User user_existed = userRepository.find(user.getId());
		
		userService.save(user);
		model.addAttribute("message", user.getEmail() + " inserted");
		return "welcome";
	}
	 
	 //using path variable
	 @GetMapping("/edit/{id}")
	  public String employeeGetFormById(@PathVariable Integer id, Model model) {

		 // TODO: check if user existed then update info else throw not found
//		 boolean user_existed = userService.get(id);
//		 if (!user_existed) {
//			 // TODO: throw new Error
//			 System.out.println("User Not Found");
//			 return "user-not-found";
//		 }
		User user = userService.get(id);
		model.addAttribute("user", user);
	    return "form-edit";
	  }

	 
	 @PostMapping("/edit/{id}")
	 public String userSubmitFormEdit(@PathVariable Integer id, @ModelAttribute("user") User user, Model model) {
		 
		System.out.println(id);

		// TODO: save user with new information
		 //

		User user_inserted = userService.findUserById(user.getId());
		userService.save(user);
		
		// TODO: .....
		model.addAttribute("message", user_inserted.getEmail() + " modified");
		return "welcome";
	  }

	  @PostMapping("/delete/{id}")
	public ModelAndView deleteUserById(@PathVariable Integer id, ModelMap model){
		  userService.delete(id);
		return new ModelAndView("redirect:/users/list", model);
	  }
//	  @PostMapping
//	public String deleteById(@PathVariable Integer id, Model model){
//		  userRepository.deleteById(id);
//		  return userList(model);
//	  }


//	@GetMapping("/list/sort")
//	public String sortUser(Model theModel) {
//		List<User> sortedUsers = new ArrayList<>();
//		sortedUsers = sortByOrderAsc(userService.findAllUsers());
//
//		int maxUsers = sortedUsers.size();
//		int maxPage = (int)Math.floor(maxUsers / numberPerPage) + 1;
//		int extraUser = maxUsers % numberPerPage;
//
//		if(maxUsers < numberPerPage){
//			userPerPage.put(1, sortedUsers.subList(0, extraUser));
//		} else{
//			for (int i = 0; i < maxPage-1; i++) {
//				userPerPage.put(i+1, sortedUsers.subList(i * numberPerPage, i * numberPerPage + numberPerPage));
//			}
//			if (extraUser > 0) {
//				userPerPage.put(maxPage,
//						sortedUsers.subList((maxPage - 1) * numberPerPage, (maxPage - 1) * numberPerPage + extraUser));
//			}
//		}
//
//		theModel.addAttribute("previousPage", 0);
//		theModel.addAttribute("nextPage", 2);
//		theModel.addAttribute("users", userPerPage.get(1));
//		theModel.addAttribute("sortType", "⇧");
//
//		theModel.addAttribute("pages", pageList(countPages(numberPerPage)));
//		return "list-user";
//	}

	//TODO: check branch develop



}
