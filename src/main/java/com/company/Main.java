package com.company;

// Original purpose of this project was to familiarize myself with FHIR and HapiFHIR.

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Main {
    static Scanner scan = new Scanner(System.in);

    static String input(String say)
    {
        System.out.println(say);
        return scan.next();
    }

    public static void main(String[] args) {

        FhirContext ctx = FhirContext.forR4();
        IGenericClient client = ctx.newRestfulGenericClient(Settings.R4_ROOT);

        String patientId = "denom-EXM104";
        String output = "";
        String choice = input("enter 'display' or 'address'");

        Patient patient = client.read().resource(Patient.class).withId(patientId).execute();

        if (choice.equalsIgnoreCase("address"))
        {
            List<Address> addresses = new ArrayList();
            List<StringType> line   = new ArrayList();
            Address address         = new Address();
            Period period           = new Period();

            System.out.println("How many lines:");
            int lineCount = scan.nextInt();

            for (int i = 0; i < lineCount; i++)
            {
                // Yo this is stupid what is this StringType nonsense
                line.add((StringType) new StringType().setValue(input(String.format("Enter data for line %d:", i))));
            }

            String use        = input("Enter use:");
            String type       = input("Enter type:");
            String text       = input("Enter text:");
            String city       = input("Enter city:");
            String district   = input("Enter district:");
            String state      = input("Enter state:");
            String postalCode = input("Enter postalcode");
            String country    = input("Enter country");

            switch (use.toLowerCase())
            {
                case "home":
                    address.setUse(Address.AddressUse.HOME);
                    break;
                case "work":
                    address.setUse(Address.AddressUse.WORK);
                    break;
                case "billing":
                    address.setUse(Address.AddressUse.BILLING);
                    break;
            }

            switch (type.toLowerCase())
            {
                case "both":
                    address.setType(Address.AddressType.BOTH);
                    break;
                case "phsyical":
                    address.setType(Address.AddressType.PHYSICAL);
                    break;
                case "postal":
                    address.setType(Address.AddressType.POSTAL);
                    break;
            }

            address.setPostalCode(postalCode);
            address.setText(text);
            address.setCity(city);
            address.setDistrict(district);
            address.setState(state);
            address.setCountry(country);
            address.setLine(line);

            period.setStart(Date.from(Instant.now()));
            period.setEnd(Date.from(Instant.now()));

            address.setPeriod(period);
            addresses.add(address);

            patient.setAddress(addresses);

            MethodOutcome outcome = client.update()
                    .resource(patient)
                    .execute();

        } else if (choice.equalsIgnoreCase("display")) {
            Date patientBirthDate;
            HumanName patientName;
            Enumerations.AdministrativeGender patientGender;
            Extension raceExtension;
            Extension ethnicExtension;

            patientBirthDate = patient.getBirthDate();
            patientName = patient.getNameFirstRep();
            patientGender = patient.getGender();

            // Yuh this seems really dumb there's got to be a better way of doing this
            raceExtension = patient.getExtensionByUrl(Settings.RACE_REF);
            Coding raceCoding = (Coding) raceExtension.getExtensionFirstRep().getValue();
            String race = raceCoding.getDisplay();

            ethnicExtension = patient.getExtensionByUrl(Settings.ETHNICITY_REF);
            Coding ethnicCoding = (Coding) ethnicExtension.getExtensionFirstRep().getValue();
            String ethnicity = ethnicCoding.getDisplay();

            // Just got away with the formatting equivalent of homicide
            output += "Patient ID -> " + patientId + " | ";
            output += "BirthDate -> " + patientBirthDate + " | ";
            output += "Name -> " + patientName.getGiven().get(0) + " " + patientName.getFamily() + " | ";
            output += "Gender -> " + patientGender + " | ";
            output += "Race -> " + race + " | ";
            output += "Ethnicity -> " + ethnicity + " | ";

            System.out.println(output);
        }
    }
}
