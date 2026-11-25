
import java.util.*;

public class CPUSchedulingSimulator {

    static class Process {
        String id;
        int arrival, burst, priority, remaining, completion, waiting, turnaround;

        Process(String id, int arrival, int burst, int priority) {
            this.id = id;
            this.arrival = arrival;
            this.burst = burst;
            this.priority = priority;
            this.remaining = burst;
        }
    }

    // Singly linked list Node
    static class Node {
        Process data;
        Node next;

        Node(Process data) {
            this.data = data;
        }
    }

    // Process Linked List
    static class ProcessList {
        Node head;

        void add(Process p) {
            Node node = new Node(p);

            if (head == null) {
                head = node;
                return;
            }

            Node current = head;

            while (current.next != null)
                current = current.next;

            current.next = node;
        }

        // Merge Sort
        void sortByArrival() {
            head = mergeSort(head);
        }

        private Node mergeSort(Node node) {
            if (node == null || node.next == null)
                return node;

            Node slow = node;
            Node fast = node.next;

            while (fast != null && fast.next != null) {
                slow = slow.next;
                fast = fast.next.next;
            }

            Node second = slow.next;
            slow.next = null;

            return merge(mergeSort(node), mergeSort(second));
        }

        private Node merge(Node a, Node b) {
            if (a == null) return b;
            if (b == null) return a;

            if (comesFirst(a.data, b.data)) {
                a.next = merge(a.next, b);
                return a;
            } else {
                b.next = merge(a, b.next);
                return b;
            }
        }

        private boolean comesFirst(Process a, Process b) {
            if (a.arrival != b.arrival)
                return a.arrival < b.arrival;

            return a.id.compareTo(b.id) <= 0;
        }

        List<Process> toList() {
            List<Process> result = new ArrayList<>();
            Node current = head;

            while (current != null) {
                result.add(current.data);
                current = current.next;
            }

            return result;
        }
    }

    // Gantt Chart Segment
    static class Segment {
        String processId;
        int start, end;

        Segment(String processId, int start, int end) {
            this.processId = processId;
            this.start = start;
            this.end = end;
        }
    }

    // Circular Queue
    static class CircularQueue {
        private final Process[] queue;
        private int front = 0, rear = 0, size = 0;

        CircularQueue(int capacity) {
            queue = new Process[capacity + 1];
        }

        boolean isEmpty() {
            return size == 0;
        }

        void offer(Process p) {
            if (size == queue.length)
                throw new IllegalStateException("Queue is full");

            queue[rear] = p;
            rear = (rear + 1) % queue.length;
            size++;
        }

        Process poll() {
            if (isEmpty())
                return null;

            Process p = queue[front];
            queue[front] = null;
            front = (front + 1) % queue.length;
            size--;

            return p;
        }
    }

    // Priority Scheduling (Non-Preemptive)
    static void priorityScheduling(List<Process> processes,
                                   List<Segment> gantt) {

        PriorityQueue<Process> ready = new PriorityQueue<>(
            Comparator.comparingInt((Process p) -> p.priority)
                .thenComparingInt(p -> p.arrival)
                .thenComparing(p -> p.id)
        );

        int time = 0, index = 0, completed = 0;

        while (completed < processes.size()) {

            while (index < processes.size()
                    && processes.get(index).arrival <= time) {
                ready.offer(processes.get(index++));
            }

            if (ready.isEmpty()) {
                time = processes.get(index).arrival;
                continue;
            }

            Process current = ready.poll();

            int start = time;
            time += current.burst;

            current.completion = time;
            current.turnaround = current.completion - current.arrival;
            current.waiting = current.turnaround - current.burst;

            gantt.add(new Segment(current.id, start, time));
            completed++;
        }
    }

    // Round Robin Scheduling
    static void roundRobin(List<Process> processes,
                           int quantum,
                           List<Segment> gantt) {

        CircularQueue ready =
            new CircularQueue(processes.size() + 1);

        int time = 0, index = 0, completed = 0;

        while (completed < processes.size()) {

            while (index < processes.size()
                    && processes.get(index).arrival <= time) {
                ready.offer(processes.get(index++));
            }

            if (ready.isEmpty()) {
                time = processes.get(index).arrival;
                continue;
            }

            Process current = ready.poll();

            int start = time;
            int runFor = Math.min(quantum, current.remaining);

            time += runFor;
            current.remaining -= runFor;

            gantt.add(new Segment(current.id, start, time));

            // Add new arrivals before re-queuing current process
            while (index < processes.size()
                    && processes.get(index).arrival <= time) {
                ready.offer(processes.get(index++));
            }

            if (current.remaining > 0) {
                ready.offer(current);
            } else {
                current.completion = time;
                current.turnaround =
                    current.completion - current.arrival;

                current.waiting =
                    current.turnaround - current.burst;

                completed++;
            }
        }
    }

    // Print Results
    static void printResults(List<Process> processes,
                             List<Segment> gantt) {

        System.out.println("\nGantt Chart:");

        for (Segment s : gantt) {
            System.out.print(
                s.processId + "(" + s.start + "-" + s.end + ") "
            );
        }

        System.out.println();

        double totalWT = 0, totalTAT = 0;

        System.out.println(
            "\nProcess\tAT\tBT\tPriority\tWT\tTAT"
        );

        for (Process p : processes) {

            System.out.printf(
                "%s\t%d\t%d\t%d\t\t%d\t%d%n",
                p.id,
                p.arrival,
                p.burst,
                p.priority,
                p.waiting,
                p.turnaround
            );

            totalWT += p.waiting;
            totalTAT += p.turnaround;
        }

        System.out.printf(
            "Average Waiting Time: %.2f ms%n",
            totalWT / processes.size()
        );

        System.out.printf(
            "Average Turnaround Time: %.2f ms%n",
            totalTAT / processes.size()
        );
    }

    // Read positive integer
    static int readPositive(Scanner sc, String prompt) {

        while (true) {
            System.out.print(prompt);

            if (sc.hasNextInt()) {
                int value = sc.nextInt();

                if (value > 0)
                    return value;

            } else {
                sc.next();
            }

            System.out.println(
                "Invalid input. Enter an integer greater than 0."
            );
        }
    }

    // Read non-negative integer
    static int readNonNegative(Scanner sc, String prompt) {

        while (true) {
            System.out.print(prompt);

            if (sc.hasNextInt()) {
                int value = sc.nextInt();

                if (value >= 0)
                    return value;

            } else {
                sc.next();
            }

            System.out.println(
                "Invalid input. Enter an integer greater than or equal to 0."
            );
        }
    }

    // Main Method
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("===== CPU SCHEDULING SIMULATOR =====");
        System.out.println("1. Priority Scheduling (Non-Preemptive)");
        System.out.println("2. Round Robin");

        int choice = readPositive(
            sc, "Choose algorithm (1 or 2): "
        );

        while (choice != 1 && choice != 2) {
            System.out.println("Choose only 1 or 2.");

            choice = readPositive(
                sc, "Choose algorithm (1 or 2): "
            );
        }

        int n = readPositive(
            sc, "Enter number of processes: "
        );

        ProcessList processList = new ProcessList();

        for (int i = 1; i <= n; i++) {

            System.out.println("\nProcess P" + i + ":");

            int at = readNonNegative(sc, "Arrival Time: ");

            int bt = readPositive(sc, "Burst Time: ");

            int pr = readPositive(
                sc,
                "Priority (lower number = higher priority): "
            );

            processList.add(
                new Process("P" + i, at, bt, pr)
            );
        }

        int quantum = 0;

        if (choice == 2) {
            quantum = readPositive(
                sc, "Enter time quantum: "
            );
        }

        processList.sortByArrival();

        List<Process> processes = processList.toList();

        System.out.print("\nSorted by arrival: ");

        for (Process p : processes) {
            System.out.print(p.id + " ");
        }

        System.out.println();

        List<Segment> gantt = new ArrayList<>();

        if (choice == 1) {
            priorityScheduling(processes, gantt);
        } else {
            roundRobin(processes, quantum, gantt);
        }

        printResults(processes, gantt);

        sc.close();
    }
}
