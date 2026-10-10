package com.example.jobportal.config;

import com.example.jobportal.model.EmployerProfile;
import com.example.jobportal.model.Job;
import com.example.jobportal.model.JobSeekerProfile;
import com.example.jobportal.model.JobType;
import com.example.jobportal.model.Role;
import com.example.jobportal.model.User;
import com.example.jobportal.model.WorkMode;
import com.example.jobportal.repository.EmployerProfileRepository;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.repository.JobSeekerProfileRepository;
import com.example.jobportal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Creates the administrator account and (optionally) demo data on first start. */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final EmployerProfileRepository employers;
    private final JobSeekerProfileRepository seekers;
    private final JobRepository jobs;
    private final PasswordEncoder encoder;

    @Value("${app.admin.name}") private String adminName;
    @Value("${app.admin.email}") private String adminEmail;
    @Value("${app.admin.password}") private String adminPassword;
    @Value("${app.seed-demo-data:false}") private boolean seedDemo;

    public DataSeeder(UserRepository users, EmployerProfileRepository employers,
                      JobSeekerProfileRepository seekers, JobRepository jobs, PasswordEncoder encoder) {
        this.users = users;
        this.employers = employers;
        this.seekers = seekers;
        this.jobs = jobs;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!users.existsByEmailIgnoreCase(adminEmail)) {
            createUser(adminName, adminEmail, adminPassword, Role.ADMIN);
            log.info("Created administrator account: {}", adminEmail);
        }
        if (seedDemo && jobs.count() == 0) {
            seedDemoData();
            log.info("Seeded demo employers, job seeker and job postings");
        }
    }

    private User createUser(String name, String email, String password, Role role) {
        User u = new User();
        u.setFullName(name);
        u.setEmail(email.toLowerCase());
        u.setPassword(encoder.encode(password));
        u.setRole(role);
        return users.save(u);
    }

    private void seedDemoData() {
        EmployerProfile tech = employer("Rahul Mehta", "hr@technova.com", "TechNova Solutions",
                "Information Technology", "201-500", "https://technova.example.com", "Hyderabad",
                "TechNova builds cloud-native products for global retail and fintech customers.");
        EmployerProfile fin = employer("Priya Nair", "careers@finedge.com", "FinEdge Analytics",
                "Financial Services", "51-200", "https://finedge.example.com", "Bengaluru",
                "FinEdge turns financial data into decisions for banks and insurers.");

        if (!users.existsByEmailIgnoreCase("anjali@example.com")) {
            User u = createUser("Anjali A", "anjali@example.com", "Seeker@123", Role.JOB_SEEKER);
            JobSeekerProfile p = new JobSeekerProfile(u);
            p.setHeadline("B.Tech AI student | Java Full Stack developer");
            p.setLocation("Hyderabad");
            p.setSkills("Java,Spring Boot,React,MySQL,REST APIs");
            p.setEducation("B.Tech, Artificial Intelligence (3rd year)");
            p.setSummary("Final-year student building full stack projects with Java, Spring Boot and React.");
            seekers.save(p);
        }

        job(tech, "Java Full Stack Developer", "Hyderabad", JobType.FULL_TIME, WorkMode.HYBRID, 0, 2,
                600000L, 1000000L, "Java,Spring Boot,React,MySQL",
                "Design and build REST APIs with Spring Boot and responsive React interfaces. You will work in an agile team, write unit tests and take part in code reviews.",
                "B.Tech/B.E. in Computer Science or related field. Strong fundamentals in OOP and SQL.");
        job(tech, "Backend Engineer (Spring Boot)", "Hyderabad", JobType.FULL_TIME, WorkMode.ONSITE, 2, 5,
                1200000L, 2000000L, "Java,Spring Boot,Microservices,Kafka,AWS",
                "Own backend services that power our retail platform. Focus on performance, reliability and clean architecture.",
                "2+ years of backend development experience with Java and Spring.");
        job(tech, "Frontend Developer (React)", "Remote", JobType.FULL_TIME, WorkMode.REMOTE, 1, 4,
                800000L, 1500000L, "React,JavaScript,CSS,Vite,Testing",
                "Create accessible, high-performance user interfaces in React. Collaborate closely with designers and backend engineers.",
                "1+ years of professional React experience.");
        job(tech, "Software Engineering Intern", "Hyderabad", JobType.INTERNSHIP, WorkMode.ONSITE, 0, 0,
                180000L, 300000L, "Java,Python,Data Structures",
                "A 6-month paid internship working alongside senior engineers on real product features.",
                "Pursuing B.Tech/B.E. in CS, IT, AI or related branch.");
        job(fin, "Data Analyst", "Bengaluru", JobType.FULL_TIME, WorkMode.HYBRID, 1, 3,
                700000L, 1200000L, "SQL,Python,Power BI,Statistics",
                "Analyse large financial datasets, build dashboards and present insights to business stakeholders.",
                "Bachelor's degree in a quantitative field.");
        job(fin, "Machine Learning Engineer", "Bengaluru", JobType.FULL_TIME, WorkMode.HYBRID, 2, 6,
                1500000L, 2800000L, "Python,TensorFlow,MLOps,SQL",
                "Develop and deploy risk and fraud detection models into production.",
                "2+ years of applied ML experience; strong Python skills.");
        job(fin, "QA Automation Engineer", "Pune", JobType.CONTRACT, WorkMode.ONSITE, 2, 5,
                600000L, 1100000L, "Selenium,Java,TestNG,CI/CD",
                "Create and maintain automated test suites for our analytics platform.",
                "Experience with Selenium or Playwright.");
        job(fin, "Product Designer (UI/UX)", "Remote", JobType.PART_TIME, WorkMode.REMOTE, 1, 4,
                400000L, 800000L, "Figma,User Research,Prototyping",
                "Design intuitive experiences for our customer-facing dashboards.",
                "Portfolio demonstrating UI/UX case studies.");
    }

    private EmployerProfile employer(String contact, String email, String company, String industry, String size,
                                     String website, String location, String description) {
        User u = createUser(contact, email, "Employer@123", Role.EMPLOYER);
        EmployerProfile p = new EmployerProfile(u, company);
        p.setIndustry(industry);
        p.setCompanySize(size);
        p.setWebsite(website);
        p.setLocation(location);
        p.setDescription(description);
        return employers.save(p);
    }

    private void job(EmployerProfile employer, String title, String location, JobType type, WorkMode mode,
                     int minExp, int maxExp, long salaryMin, long salaryMax, String skills,
                     String description, String eligibility) {
        Job j = new Job();
        j.setEmployer(employer);
        j.setTitle(title);
        j.setLocation(location);
        j.setJobType(type);
        j.setWorkMode(mode);
        j.setMinExperience(minExp);
        j.setMaxExperience(maxExp);
        j.setSalaryMin(salaryMin);
        j.setSalaryMax(salaryMax);
        j.setSkills(skills);
        j.setDescription(description);
        j.setEligibility(eligibility);
        j.setOpenings(2);
        j.setDeadline(LocalDate.now().plusDays(45));
        jobs.save(j);
    }
}
