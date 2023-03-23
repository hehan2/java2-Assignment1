import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is just a demo for you, please run it on JDK17 (some statements may be not allowed in lower version).
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {
    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
                        Integer.parseInt(info[6]), Integer.parseInt(info[7]),
                        Integer.parseInt(info[8]),
                        Integer.parseInt(info[9]), Integer.parseInt(info[10]),
                        Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]), Double.parseDouble(info[13]),
                        Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]), Double.parseDouble(info[16]),
                        Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]), Double.parseDouble(info[19]),
                        Double.parseDouble(info[20]),
                        Double.parseDouble(info[21]), Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
        return courses.stream().collect(Collectors.groupingBy(c -> c.institution, TreeMap::new, Collectors.mapping(c -> c.participants, Collectors.summingInt(Integer::intValue))));
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        return courses.stream()
                .collect(Collectors.groupingBy(c -> c.institution + "-" + c.subject,
                        TreeMap::new, Collectors.summingInt(c -> c.participants)))
                .entrySet().stream().sorted((o1, o2) -> o2.getValue() - o1.getValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String, List<List<String>>> map = new HashMap<>();
        courses.stream().sorted(Comparator.comparing(o -> o.title)).forEach(course -> {
            List<String> inss = Arrays.stream(course.instructors.trim().split(","))
                    .map(String::trim).toList();
                for (String ins :
                        inss) {
                    if (map.containsKey(ins)) {
                        if (inss.size() == 1){
                            if (!map.get(ins).get(0).contains(course.title)){
                                map.get(ins).get(0).add(course.title);
                            }
                        }
                        else {
                            if (!map.get(ins).get(1).contains(course.title)){
                                map.get(ins).get(1).add(course.title);
                            }
                        }
                    }
                    else {
                        map.put(ins, Arrays.asList(new ArrayList<>(), new ArrayList<>()));
                        if (inss.size() == 1){
                            if(!map.get(ins).get(0).contains(course.title))
                            map.get(ins).get(0).add(course.title);
                        }
                        else{
                            if (!map.get(ins).get(1).contains(course.title))
                            map.get(ins).get(1).add(course.title);
                        }
                    }
                    }
        });
        return map;

    }

    //4
    public List<String> getCourses(int topK, String by) {
        if (by.equals("hours")){
            return courses.stream().sorted((o1, o2) -> (int) (o2.totalHours - o1.totalHours)).map(course -> course.title).distinct().limit(topK).toList();
        }
        else{
            return courses.stream().sorted((o1, o2) -> (o2.participants - o1.participants)).map(course -> course.title).distinct().limit(topK).toList();
        }

    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        return courses.stream()
                .filter(course -> course.subject.toLowerCase().contains(courseSubject.toLowerCase()))
                .filter(course -> course.percentAudited >= percentAudited)
                .filter(course -> course.totalHours <= totalCourseHours)
                .map(course -> course.title)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        Map<String, Double> aveargeMedian = courses.stream().collect(Collectors.groupingBy(course -> course.number, Collectors.averagingDouble(course -> course.medianAge)));
        Map<String, Double> aveargeMale = courses.stream().collect(Collectors.groupingBy(course -> course.number, Collectors.averagingDouble(course -> course.percentMale)));
        Map<String, Double> aveargeDegree = courses.stream().collect(Collectors.groupingBy(course -> course.number, Collectors.averagingDouble(course -> course.percentDegree)));
        Map<String, Course> latestCourse = courses.stream()
                .collect(Collectors.toMap(course -> course.number, Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparing(course -> course.launchDate))));

        return courses.stream().collect(Collectors.toMap(course -> latestCourse.get(course.number).title, (course ->
                (int)(Math.pow(age - aveargeMedian.get(course.number), 2) + Math.pow(gender * 100 - aveargeMale.get(course.number), 2) + Math.pow(isBachelorOrHigher * 100 - aveargeDegree.get(course.number),2)
                )), (o1, o2) -> o1)).entrySet().stream().sorted((Comparator.comparingInt(Map.Entry::getValue))).map(Map.Entry::getKey).limit(10).toList();
    }

}

class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }


}