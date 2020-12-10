
import java.util.List;
import java.util.Scanner;

/**
 * Created by Lisa Ramel
 * Date: 2020-12-04
 * Time: 14:03
 * Project: Preeschool
 * Copywrite: MIT
 */
public class Main {

    private final Database d = new Database();

    private AttendanceDAO attendanceDAO = d;
    private DatabaseDAO databaseDAO = d;
    private PersonDAO personDAO = d;

    private Scanner scan = new Scanner(System.in);

    private States s;

/*
1. Välj Vårdnadshavare eller Pedagog ex (1,2)

Vårdnadshavare
1. Välj barn
1. Registerara frånvaro
2. Anmäla omsorgstider
3. Visa pedagogers uppgifter

Pedagog
1. Lägga till frånvaro
2. Registrera nytt barn

 */

    public Main() {
        s = States.LOGIN;
        s.output(null);
        int input = scan.nextInt();


        while (true) {
            if (input == 1) {
                caregiverView(input);
                s = States.LOGIN;
                s.output(null);
                input = scan.nextInt();
                //break;
            } else if (input == 2) {
                educatorView(input);
                s = States.LOGIN;
                s.output(null);
                input = scan.nextInt();
                //break;
            } else if (input == 3) {
                s = States.SHUT_DOWN;
                s.output(null);
                saveAllFiles();
                break;
            } else {
                System.out.println("Ogiltigt kommando, var god försök igen.");
                input = scan.nextInt();
            }
        }
    }

    public Caregiver lookupCaregiver(String name) {
        Caregiver caregiver = personDAO.getCaregiver(name);
        while (caregiver == null) {
            System.out.println("Var god försök igen: ");
            name = scan.next();
            caregiver = personDAO.getCaregiver(name);
        }
        return caregiver;
    }

    public void caregiverView(int input) {
        String name;
        //Om användaren valde att logga in som vårdnadshavare (1)

        s = States.USERNAME;
        s.output(null);
        name = scan.next();
        Caregiver caregiver = lookupCaregiver(name);
        while (true) {
            Child child;
            child = caregiver.getChildren().get(0);

            if (caregiver.getChildren().size() > 1) {
                s = States.CAREGIVER;
                s.output(caregiver);
                // väljer barn
                input = scan.nextInt();
                //Om användaren valde ett barn (1)
                if (input <= caregiver.getChildren().size()) {
                    child = caregiver.getChildren().get(input - 1);
                }
            }

            s = States.CHOSE_CHILD;
            s.output(child);

            input = scan.nextInt();

            //Om användaren valde omsorgstider (1)
            if (input == 1) {
                s = States.CHILD_ATTENDANCE;
                s.output(child);
                s.addCaringTime(child, scan);
            }

            //Om användaren valde frånvaro (2)
            else if (input == 2) {
                s = States.CHILD_ABSENCE;
                addAbsenseToday(child);
            }

            //Om användaren valde kontaktuppgifter (3)
            else if (input == 3) {
                s = States.EDUCATOR_INFO;
                List<Educator> educatorList = databaseDAO.getEducatorList();
                s.output(educatorList);
            }
            //Om användaren valde att Logga ut (4)
            else if (input == 4) {
                s = States.LOG_OUT;
                s.output(caregiver);
                break;
            }

            else {
                System.out.println("Okänt kommando, var göd försök igen.");
            }
        }

    }


    public void educatorView(int input) {

        String name;
        String firstName;

        //Om användaren valde att logga in som pedagog (2)

        s = States.USERNAME;
        s.output(null);

        name = scan.next();
        Educator educator = personDAO.getEducator(name);
        while (educator == null) {
            System.out.println("Var god försök igen: ");
            name = scan.next();
            educator = personDAO.getEducator(name);
        }

        while (true) {
            s = States.EDUCATOR;
            s.output(educator);
            input = scan.nextInt();

            //Om användaren valde att registrera frånvaro för ett barn
            if (input == 1) {
                s = States.EDUCATOR_ABSENCE;
                List<Child> childList = databaseDAO.getChildList();
                s.output(databaseDAO.getChildList());
                input = scan.nextInt();

                //Registrerar frånvaro på barn
                if (input <= childList.size()) {
                    s = States.CHILD_ABSENCE;
                    Child child = childList.get(input - 1);
                    addAbsenseToday(child);
                }


                //Om användaren vill lägga till ett barn
            } else if (input == 2) {

                state = States.REGISTER_CHILD;
                state.output(null);
                firstName = scan.next();
                boolean foundCaregiver = false;

                //Om vårdnadshavaren redan finns i systemet, läggs
                //ett nytt barn läggs till till den redan exsisterande vårdnadshavaren
                for (Caregiver caregiver : d.getCaregiverList()) {
                    if (caregiver.getFirstName().equalsIgnoreCase(firstName)) {
                        System.out.println("Det nya barnet kommer att registreras på den redan " +
                                "\nexisterande vårdnadshavaren " + caregiver.getFirstName() + " " + caregiver.getLastName());

                        Child child = state.registerNewChild(scan);

                        caregiver.addChildren(child);
                        d.addChild(child);
                        //s.registerNewChild(scan);
                        foundCaregiver = true;
                    }
                }

                //Om det är en ny vårdnadshavare så adderas en ny vårdnadshavare
                //och ett nytt barn läggs till och kopplas till den nya vårdnadshavaren
                if (!foundCaregiver) {

                    Caregiver caregiver = state.addCaregiverToNewChild(scan, firstName);
                    Child child = state.registerNewChild(scan);

                    d.addChild(child);
                    d.addCaregiver(caregiver);
                    caregiver.addChildren(child);
                }

                //TODO test om barn lagts till:
                System.out.println(d.getChildList().size());
                System.out.println(d.getCaregiverList().size());

            }
            //Om användaren vill skriva ut närvarolistor
            else if (input == 3) {
                List<Attendance> attendanceList = d.deSerialize(SerFiles.ATTENDANCE.serFiles);
                s = States.ATTENDANCE;
                s.output(null);
                input = scan.nextInt();
                if (input == 1) {

                    s = States.PRINT_ALL;
                    s.output(attendanceList);
                } else if (input == 2) {
                    s = States.PRINT_PRESENT;
                    s.output(attendanceList);
                } else if (input == 3) {
                    s = States.PRINT_ABSENT;
                    s.output(attendanceList);
                }
            }
             
            else if (input == 4) {
                s = States.CAREGIVER_INFO;
                s.output(null);
                name = scan.next();
                List<Child> childList = databaseDAO.getChildList();
                for (Child child : childList) {
                    if (name.equalsIgnoreCase(child.getFirstName()) || name.equalsIgnoreCase(child.getLastName())) {
                        s = States.CAREGIVER_INFO_PRINT;
                        s.output(child);
                    }
                }
            }
            //Om användaren valde att Logga ut (5)
            else if (input == 5) {
                s = States.LOG_OUT;
                s.output(educator);
                break;

            }

            else {
                System.out.println("Okänt kommando, var god försök igen.");
            }

        }
    }
    public void addAbsenseToday(Child child){
        s.output(child);
        attendanceDAO.addAbsence(child);
        d.serialize(d.getAttendanceToday(), SerFiles.ATTENDANCE.serFiles);
    }

    public void saveAllFiles(){
        d.addAttendanceTodayInList(d.getAttendanceToday());
        d.serialize(d.getAttendanceList(), SerFiles.LIST_OF_ATTENDANCES.serFiles);
        d.serialize(d.getAttendanceToday(), SerFiles.ATTENDANCE.serFiles);
        d.serialize(d.getChildList(), SerFiles.CHILDREN.serFiles);
        d.serialize(d.getEducatorList(), SerFiles.EDUCATOR.serFiles);
    }

    public static void main(String[] args) {
        new Main();
    }
}



