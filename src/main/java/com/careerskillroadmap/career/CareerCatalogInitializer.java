package com.careerskillroadmap.career;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CareerCatalogInitializer implements ApplicationRunner {

    private final CareerRepository careerRepository;

    public CareerCatalogInitializer(CareerRepository careerRepository) {
        this.careerRepository = careerRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (CareerSeed seed : careerCatalog()) {
            if (careerRepository.findBySlug(seed.slug()).isEmpty()) {
                Career career = new Career(seed.title(), seed.domain(), seed.description(), seed.slug());
                for (int index = 0; index < seed.skills().size(); index++) {
                    SkillSeed skill = seed.skills().get(index);
                    career.addSkill(new Skill(skill.title(), skill.description(), index + 1));
                }
                careerRepository.save(career);
            }
        }
    }

    private List<CareerSeed> careerCatalog() {
        return List.of(
                new CareerSeed("Electronics and Communication Engineer", "ECE",
                        "Design, test, and improve electronic circuits and communication systems.",
                        "electronics-communication-engineer", List.of(
                                new SkillSeed("Circuit fundamentals", "Analyze analog and digital circuits and common components."),
                                new SkillSeed("Signals and communication", "Understand signals, modulation, and communication links."),
                                new SkillSeed("PCB design and testing", "Create circuit layouts and validate boards with lab instruments."))),
                new CareerSeed("Embedded Systems Engineer", "Embedded Systems",
                        "Build dependable software and firmware for microcontrollers and electronic devices.",
                        "embedded-systems-engineer", List.of(
                                new SkillSeed("C programming", "Use C, pointers, memory, and bitwise operations for firmware."),
                                new SkillSeed("Microcontrollers and peripherals", "Work with GPIO, timers, interrupts, and ADC peripherals."),
                                new SkillSeed("Firmware debugging", "Debug embedded programs with serial tools and hardware probes."))),
                new CareerSeed("IoT Solutions Engineer", "IoT",
                        "Connect sensors and devices to networks and cloud services for real-world applications.",
                        "iot-solutions-engineer", List.of(
                                new SkillSeed("Sensors and device interfaces", "Read sensor data and connect common device interfaces."),
                                new SkillSeed("Wireless and MQTT", "Connect devices using Wi-Fi and exchange data with MQTT."),
                                new SkillSeed("IoT prototyping", "Build a connected-device prototype and document its data flow."))),
                new CareerSeed("VLSI Design and Verification Engineer", "VLSI",
                        "Design and verify digital logic used in integrated circuits and semiconductor devices.",
                        "vlsi-design-verification-engineer", List.of(
                                new SkillSeed("Digital logic design", "Apply Boolean algebra, combinational logic, and sequential circuits."),
                                new SkillSeed("Verilog and RTL", "Describe digital hardware with synthesizable Verilog RTL."),
                                new SkillSeed("Design verification", "Create testbenches and verify expected hardware behavior."))),
                new CareerSeed("Robotics and Automation Engineer", "Robotics",
                        "Develop robotic systems that sense, plan, and act in physical environments.",
                        "robotics-automation-engineer", List.of(
                                new SkillSeed("Robot kinematics", "Understand coordinate frames, motion, and robot kinematics."),
                                new SkillSeed("Sensors and actuators", "Integrate sensors, motors, and actuator control."),
                                new SkillSeed("ROS and robot programming", "Build and test a basic robotics application using ROS concepts."))),
                new CareerSeed("Electrical Systems Engineer", "Electrical",
                        "Design, operate, and maintain electrical power systems and equipment.",
                        "electrical-systems-engineer", List.of(
                                new SkillSeed("Electrical circuit analysis", "Analyze AC and DC circuits and electrical measurements."),
                                new SkillSeed("Power systems", "Learn generation, transmission, distribution, and protection basics."),
                                new SkillSeed("Electrical safety and testing", "Apply safe test procedures and interpret equipment results."))),
                new CareerSeed("Software Developer", "Software Development",
                        "Create, test, and maintain software applications that solve user and business problems.",
                        "software-developer", List.of(
                                new SkillSeed("Programming fundamentals", "Practice variables, control flow, functions, and data structures."),
                                new SkillSeed("Web development", "Build accessible web pages and interactive user interfaces."),
                                new SkillSeed("Git and software testing", "Track changes with Git and verify behavior with tests."))),
                new CareerSeed("AI and Machine Learning Engineer", "AI/ML",
                        "Build machine-learning solutions that learn patterns from data and support useful decisions.",
                        "ai-machine-learning-engineer", List.of(
                                new SkillSeed("Python for data", "Use Python and common data tools to explore and prepare datasets."),
                                new SkillSeed("Machine-learning fundamentals", "Train and evaluate introductory supervised learning models."),
                                new SkillSeed("Model evaluation and deployment", "Measure model performance and package a small prediction service."))),
                new CareerSeed("Cybersecurity Analyst", "Cybersecurity",
                        "Help protect systems and data by identifying risks, investigating alerts, and improving defenses.",
                        "cybersecurity-analyst", List.of(
                                new SkillSeed("Networking and security basics", "Understand network protocols, common threats, and defensive controls."),
                                new SkillSeed("Linux and security tools", "Use command-line tools to inspect systems and security events."),
                                new SkillSeed("Incident analysis", "Triage alerts and write a clear, evidence-based incident report."))),
                new CareerSeed("Cloud and Networking Engineer", "Cloud/Networking",
                        "Connect, deploy, and operate reliable networked services on modern cloud platforms.",
                        "cloud-networking-engineer", List.of(
                                new SkillSeed("Networking fundamentals", "Learn IP addressing, routing, DNS, and network troubleshooting."),
                                new SkillSeed("Cloud computing basics", "Deploy and configure a small service using cloud concepts."),
                                new SkillSeed("Linux and automation", "Manage Linux services and automate repeatable setup tasks."))),
                new CareerSeed("Data Scientist", "Data Science",
                        "Use statistics, programming, and visualization to uncover patterns and communicate insights.",
                        "data-scientist", List.of(
                                new SkillSeed("Python and data analysis", "Clean, transform, and summarize datasets with Python."),
                                new SkillSeed("Statistics and visualization", "Use descriptive statistics and communicate findings with charts."),
                                new SkillSeed("Applied machine learning", "Build and evaluate a simple model for a practical data question."))),
                new CareerSeed("UI/UX Designer", "UI/UX",
                        "Research user needs and design intuitive, accessible digital product experiences.",
                        "ui-ux-designer", List.of(
                                new SkillSeed("User research", "Plan interviews and synthesize user needs into clear insights."),
                                new SkillSeed("Wireframes and prototyping", "Create wireframes and interactive prototypes for a user flow."),
                                new SkillSeed("Usability and accessibility", "Evaluate usability and apply inclusive design fundamentals.")))
        );
    }

    private record CareerSeed(String title, String domain, String description, String slug, List<SkillSeed> skills) {
    }

    private record SkillSeed(String title, String description) {
    }
}
