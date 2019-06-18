package org.javamaster.b2c.test.model.validation;


import org.javamaster.b2c.test.validation.CarChecks;
import org.javamaster.b2c.test.validation.RentalChecks;

import javax.validation.GroupSequence;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * @author yudong
 * @date 2019/6/17
 */
@GroupSequence({RentalChecks.class, CarChecks.class, RentalCar.class})
public class RentalCar extends Car {
    @AssertFalse(message = "The car is currently rented out", groups = RentalChecks.class)
    private boolean rentalStation = true;

    public RentalCar() {
        super();
    }

    public RentalCar(String manufacturer, String licensePlate, int seatCount, boolean isRegistered,
                     boolean passedVehicleInspection, Driver driver, List<Person> passengers) {
        super(manufacturer, licensePlate, seatCount, isRegistered, passedVehicleInspection, driver, passengers);
    }

    @NotNull
    public boolean getRentalStation() {
        return rentalStation;
    }

    public void setRentalStation(boolean rentalStation) {
        this.rentalStation = rentalStation;
    }
}
