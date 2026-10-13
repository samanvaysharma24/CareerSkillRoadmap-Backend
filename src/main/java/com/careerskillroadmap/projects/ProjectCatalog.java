package com.careerskillroadmap.projects;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class ProjectCatalog {

    private static ProjectDefinition project(
            String key, String title, String description, String domain, String difficulty,
            List<String> tools, String outcome, String... steps) {
        List<ProjectDefinition.MilestoneDefinition> milestones = java.util.stream.IntStream.range(0, steps.length)
                .mapToObj(index -> new ProjectDefinition.MilestoneDefinition(
                        "step-" + (index + 1), "Milestone " + (index + 1), steps[index]))
                .toList();
        Set<String> goals = switch (domain) {
            case "software", "ai" -> Set.of("projects", "career-ready", "analysis", "research");
            case "electrical", "ece", "embedded", "iot", "robotics", "vlsi" ->
                    Set.of("projects", "hands-on", "research", "career-ready");
            default -> Set.of("projects", "career-ready");
        };
        return new ProjectDefinition(key, title, description, domain, difficulty, tools, milestones, outcome, goals);
    }

    private final List<ProjectDefinition> projects = List.of(
            project("ece-signal-lab", "Signal Sampling Lab", "Explore sampling and filtering with generated signals.",
                    "ece", "Beginner", List.of("Python", "NumPy", "Jupyter"), "A notebook comparing sampled and filtered signals.",
                    "Generate clean sine and square waves.", "Sample signals at different rates.", "Plot and explain aliasing."),
            project("ece-digital-link", "Digital Link Simulator", "Model a small digital link and measure bit errors.",
                    "ece", "Intermediate", List.of("Python", "NumPy", "communications basics"), "A documented link simulation with error-rate plots.",
                    "Encode a test bit stream.", "Add noise and decode the stream.", "Measure errors across signal-to-noise ratios."),
            project("ece-spectrum-tool", "Spectrum Analysis Toolkit", "Build a small spectrum analysis workflow for real or generated data.",
                    "ece", "Advanced", List.of("Python", "SciPy", "FFT"), "A reusable analysis tool with documented test data.",
                    "Design the analysis pipeline.", "Validate FFT and windowing choices.", "Compare measured data with a reference model."),
            project("embedded-ledger", "Microcontroller Sensor Logger", "Collect sensor readings and report them reliably.",
                    "embedded", "Beginner", List.of("C", "Arduino or ESP32", "temperature sensor"), "A working logger with a short setup guide.",
                    "Read a sensor and inspect its range.", "Record timestamped readings.", "Document wiring and test results."),
            project("embedded-device-driver", "Peripheral Driver Module", "Create a small driver with a clear interface and repeatable tests.",
                    "embedded", "Intermediate", List.of("C", "microcontroller", "logic analyzer"), "A tested driver module and integration notes.",
                    "Define driver states and API.", "Implement and exercise normal/error paths.", "Measure timing and document limitations."),
            project("embedded-rtos", "RTOS Telemetry Node", "Build a multitask telemetry node with bounded message handling.",
                    "embedded", "Advanced", List.of("C", "FreeRTOS", "UART"), "A demonstrable RTOS node with timing measurements.",
                    "Partition work into tasks and queues.", "Implement synchronization and recovery.", "Stress-test scheduling and record results."),
            project("iot-room-monitor", "Connected Room Monitor", "Send temperature and humidity readings to a local dashboard.",
                    "iot", "Beginner", List.of("ESP32", "MQTT", "sensor"), "A live local dashboard with device setup instructions.",
                    "Read and validate sensor values.", "Publish readings to an MQTT broker.", "Display recent readings and document setup."),
            project("iot-device-registry", "IoT Device Registry", "Track device metadata and health in a small service.",
                    "iot", "Intermediate", List.of("Java or Python", "REST", "MQTT"), "A service demo with device status and API examples.",
                    "Define a device data model.", "Ingest heartbeats and validate payloads.", "Expose health queries and test stale-device handling."),
            project("iot-edge-alerts", "Edge Anomaly Alerts", "Detect unusual sensor readings before forwarding summaries.",
                    "iot", "Advanced", List.of("Python", "MQTT", "edge device"), "An edge demo with a measured alert policy.",
                    "Collect a representative sample stream.", "Implement and tune anomaly rules.", "Compare alert quality and resource use."),
            project("vlsi-counter", "Parameterized RTL Counter", "Design, simulate, and verify a configurable counter.",
                    "vlsi", "Beginner", List.of("SystemVerilog", "Verilator", "waveforms"), "RTL, testbench, and waveform evidence.",
                    "Write a parameterized counter module.", "Create reset and boundary tests.", "Review waveforms and summarize behavior."),
            project("vlsi-fifo", "Synchronous FIFO Controller", "Implement a FIFO with clear full/empty behavior and verification.",
                    "vlsi", "Intermediate", List.of("SystemVerilog", "simulation", "assertions"), "Verified FIFO RTL with coverage notes.",
                    "Define pointer and flag behavior.", "Build directed and randomized tests.", "Check corner cases and summarize verification."),
            project("vlsi-arbiter", "Round-Robin Bus Arbiter", "Design an arbiter and test fairness under changing requests.",
                    "vlsi", "Advanced", List.of("SystemVerilog", "formal properties", "simulation"), "Arbiter RTL with fairness evidence.",
                    "Specify request/grant protocol.", "Implement round-robin state and assertions.", "Stress fairness and latency cases."),
            project("robotics-line-follower", "Line-Following Robot", "Build a simple robot that follows a marked path.",
                    "robotics", "Beginner", List.of("microcontroller", "IR sensors", "motor driver"), "A repeatable robot demo with tuning notes.",
                    "Assemble and test sensor inputs.", "Drive motors from calibrated readings.", "Tune behavior and record a successful run."),
            project("robotics-mapping", "2D Robot Mapping Demo", "Combine simulated robot motion and range sensing into a basic map.",
                    "robotics", "Intermediate", List.of("Python", "ROS 2", "range sensor"), "A map demo with reproducible simulation instructions.",
                    "Set up a robot and sensor simulation.", "Transform sensor readings into map coordinates.", "Evaluate mapping quality on a test route."),
            project("robotics-motion-planner", "Motion Planning Benchmark", "Compare collision-free planners in a small simulated environment.",
                    "robotics", "Advanced", List.of("Python", "ROS 2", "planning algorithms"), "A benchmark report with repeatable scenarios.",
                    "Define obstacles and evaluation metrics.", "Implement and compare planner options.", "Measure path quality and failure cases."),
            project("electrical-load-study", "Household Load Study", "Analyze sample electrical loads and produce a safe measurement plan.",
                    "electrical", "Beginner", List.of("spreadsheets", "power basics", "safety procedures"), "A load summary with a safety-first measurement plan.",
                    "List sample loads and nameplate ratings.", "Estimate daily energy use.", "Present findings and safe measurement boundaries."),
            project("electrical-motor-monitor", "Motor Condition Monitor", "Analyze motor operating signals to identify changing conditions.",
                    "electrical", "Intermediate", List.of("Python", "current sensor", "signal analysis"), "A tested monitor with a concise analysis report.",
                    "Collect or generate current samples.", "Extract useful operating features.", "Compare normal and changed-condition signals."),
            project("electrical-microgrid", "Microgrid Dispatch Model", "Model dispatch decisions for a small renewable microgrid.",
                    "electrical", "Advanced", List.of("Python", "optimization", "power systems"), "A scenario model with documented assumptions.",
                    "Define generation, load, and constraints.", "Implement a dispatch objective.", "Compare scenarios and explain limitations."),
            project("software-task-api", "Task API Service", "Build a small API with validation and automated tests.",
                    "software", "Beginner", List.of("Java", "Spring Boot", "JUnit"), "A documented API with passing tests.",
                    "Define a small task resource.", "Implement validated create and list routes.", "Add tests and a run guide."),
            project("software-event-platform", "Event Booking Service", "Create a tested service for events, seats, and booking rules.",
                    "software", "Intermediate", List.of("Java", "Spring Boot", "SQL"), "A service demo with tested booking constraints.",
                    "Model events and seat availability.", "Implement transactional booking rules.", "Test concurrency-sensitive cases."),
            project("software-observability", "Observable Web Service", "Instrument a service and diagnose controlled failure scenarios.",
                    "software", "Advanced", List.of("Java", "metrics", "structured logging"), "A service with dashboards and a troubleshooting report.",
                    "Add request and health metrics.", "Introduce controlled failure scenarios.", "Analyze traces/logs and document mitigations."),
            project("ai-image-classifier", "Image Classifier Baseline", "Train and evaluate a small image classifier with a clear baseline.",
                    "ai", "Beginner", List.of("Python", "scikit-learn", "Jupyter"), "A reproducible notebook with evaluation metrics.",
                    "Inspect a small labeled dataset.", "Train a baseline model.", "Evaluate errors and document limits."),
            project("ai-recommendation-model", "Recommendation Prototype", "Compare simple recommendation strategies on sample data.",
                    "ai", "Intermediate", List.of("Python", "pandas", "scikit-learn"), "A prototype with offline ranking metrics.",
                    "Prepare interactions and a baseline.", "Implement content and collaborative approaches.", "Compare results and note cold-start limits."),
            project("ai-model-monitor", "Model Drift Monitor", "Track data and prediction changes with a reproducible monitoring demo.",
                    "ai", "Advanced", List.of("Python", "pandas", "model evaluation"), "A monitoring report with alert thresholds.",
                    "Create a stable reference dataset.", "Simulate input and prediction drift.", "Evaluate alert thresholds and false alarms.")
    );

    public List<ProjectDefinition> all() {
        return projects;
    }

    public ProjectDefinition find(String key) {
        return projects.stream().filter(project -> project.key().equals(key)).findFirst()
                .orElseThrow(() -> new ProjectNotFoundException(key));
    }
}
