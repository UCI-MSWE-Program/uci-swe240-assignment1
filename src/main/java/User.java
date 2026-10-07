import java.util.Objects;

/**
 * A bank customer holding personal and address information.
 *
 * <p>Instances are created through the {@link Builder}, which enforces the
 * required fields (first name, last name, street, state, zip and ssn).
 * Setters validate and normalize input: street addresses must begin with a
 * number, units are alphanumeric, states must be US state abbreviations,
 * zips are numeric, names are truncated to 35 characters, streets to 46,
 * and ssn values must match {@code XXX-XX-XXXX}. Equality is value-based
 * over all fields, and the hash code is cached and invalidated on every
 * mutation.</p>
 */
public class User {
    /** The set of valid US state abbreviations. */
    private final Array<String> VALID_STATES = Array.of("AL","AK","AZ","AR","CA","CO","CT","DE","FL","GA","HI","ID","IL","IN","IA","KS","KY","LA","ME","MD","MA","MI","MN","MS","MO","MT","NE","NV","NH","NJ","NM","NY","NC","ND","OH","OK","OR","PA","RI","SC","SD","TN","TX","UT","VT","VA","WA","WV","WI","WY");
    /** Cached hash code; {@code null} until first computed, then reset to {@code null} by every setter. */
    private Integer hash;
    private String firstName;
    private String middleName;
    private String lastName;
    private String street;
    private String unit;
    private String state;
    private String zip;
    private String ssn;

    /**
     * Private so that instances are created exclusively through the
     * {@link Builder}.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     */
    private User() {}

    /**
     * Builder for constructing {@link User} instances.
     *
     * <p>Each {@code with}-style method validates and stores a single field
     * and returns the builder for chaining. {@link #build()} requires first
     * name, last name, street, state, zip and ssn to have been set; unit
     * and middle name are optional.</p>
     */
    public static class Builder {
        private User user = new User();

        /**
         * Creates a new, empty builder.
         *
         * Time Complexity: O(1).
         * Space Complexity: O(1).
         */
        public Builder() {}

        /**
         * Sets the user's first name.
         *
         * Time Complexity: O(m) where m is the length of the given name.
         * Space Complexity: O(1).
         *
         * @param name the first name
         * @return this builder, for chaining
         */
        public Builder firstName(String name) {
            user.setFirstName(name);
            return this;
        }

        /**
         * Sets the user's middle name; ignored if {@code null} or blank.
         *
         * Time Complexity: O(m) where m is the length of the given name.
         * Space Complexity: O(1).
         *
         * @param name the middle name
         * @return this builder, for chaining
         */
        public Builder middleName(String name) {
            user.setMiddleName(name);
            return this;
        }

        /**
         * Sets the user's last name.
         *
         * Time Complexity: O(m) where m is the length of the given name.
         * Space Complexity: O(1).
         *
         * @param name the last name
         * @return this builder, for chaining
         */
        public Builder lastName(String name) {
            user.setLastName(name);
            return this;
        }

        /**
         * Sets the user's street address.
         *
         * Time Complexity: O(m) where m is the length of the given street.
         * Space Complexity: O(1).
         *
         * @param street the street address
         * @return this builder, for chaining
         */
        public Builder street(String street) {
            user.setStreet(street);
            return this;
        }

        /**
         * Sets the user's unit (apartment, suite, etc.).
         *
         * Time Complexity: O(m) where m is the length of the given unit.
         * Space Complexity: O(1).
         *
         * @param unit the unit
         * @return this builder, for chaining
         */
        public Builder unit(String unit) {
            user.setUnit(unit);
            return this;
        }

        /**
         * Sets the user's state abbreviation.
         *
         * Time Complexity: O(1).
         * Space Complexity: O(1).
         *
         * @param state the state abbreviation
         * @return this builder, for chaining
         */
        public Builder state(String state) {
            user.setState(state);
            return this;
        }

        /**
         * Sets the user's zip code.
         *
         * Time Complexity: O(m) where m is the length of the given zip.
         * Space Complexity: O(1).
         *
         * @param zip the zip code
         * @return this builder, for chaining
         */
        public Builder zip(String zip) {
            user.setZip(zip);
            return this;
        }

        /**
         * Sets the user's social security number.
         *
         * Time Complexity: O(1).
         * Space Complexity: O(1).
         *
         * @param ssn the ssn in {@code XXX-XX-XXXX} format
         * @return this builder, for chaining
         */
        public Builder ssn(String ssn) {
            user.setSsn(ssn);
            return this;
        }

        /**
         * Builds and returns the configured user.
         *
         * <p>First name, last name, street, state, zip and ssn are required;
         * if any is missing, the returned exception message lists every
         * missing field.</p>
         *
         * Time Complexity: O(1).
         * Space Complexity: O(1).
         *
         * @return the configured user
         * @throws IllegalStateException if any required field was not set
         */
        public User build() {
            String errorPrefix = "Missing parameters: ";
            StringBuilder errorMessage = new StringBuilder(errorPrefix);
            if (user.firstName == null) {
                errorMessage.append("first name, ");
            }
            if (user.lastName == null) {
                errorMessage.append("last name, ");
            }
            if (user.street == null) {
                errorMessage.append("street, ");
            }
            if (user.state == null) {
                errorMessage.append("state, ");
            }
            if (user.zip == null) {
                errorMessage.append("zip, ");
            }
            if (user.ssn == null) {
                errorMessage.append("ssn, ");
            }
            if (errorMessage.length() > errorPrefix.length()) {
                errorMessage.setLength(errorMessage.length() - 2);
                throw new IllegalStateException(errorMessage.toString());
            }
            return user;
        }
    }

    /**
     * Returns the street address.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return the street address
     */
    public String getStreet() {
        return street;
    }

    /**
     * Returns the unit (apartment, suite, etc.), or {@code null} if unset.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return the unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Returns the state abbreviation.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return the state abbreviation
     */
    public String getState() {
        return state;
    }

    /**
     * Returns the zip code.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return the zip code
     */
    public String getZip() {
        return zip;
    }

    /**
     * Returns the first name.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the middle name, or {@code null} if unset.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return the middle name
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * Returns the last name.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the social security number.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return the ssn in {@code XXX-XX-XXXX} format
     */
    public String getSsn() {
        return ssn;
    }

    /**
     * Sets the street address.
     *
     * <p>The raw value must start with a number and be followed by
     * alphanumeric characters and spaces. The stored value is the trimmed
     * value truncated to 46 characters.</p>
     *
     * Time Complexity: O(m) where m is the length of the given street.
     * Space Complexity: O(1).
     *
     * @param street the street address
     * @throws AssertionError          if {@code street} is {@code null}
     * @throws IllegalArgumentException if {@code street} does not start with
     *                                  a number
     */
    public void setStreet(String street) {
        assert street != null;
        if (!street.matches("^\\d+[a-zA-Z ]+$")) {
            throw new IllegalArgumentException("Street address must start with a number and be followed by alphanumeric characters.");
        }
        this.street = street.substring(0, Math.min(46, street.length()));
        hash = null;
    }

    /**
     * Sets the unit (apartment, suite, etc.).
     *
     * <p>The value is trimmed and must contain only alphanumeric characters
     * and spaces.</p>
     *
     * Time Complexity: O(m) where m is the length of the given unit.
     * Space Complexity: O(1).
     *
     * @param unit the unit
     * @throws AssertionError          if {@code unit} is {@code null}
     * @throws IllegalArgumentException if {@code unit} contains characters
     *                                  other than alphanumerics and spaces
     */
    public void setUnit(String unit) {
        assert unit != null;
        unit = unit.trim();
        if (!unit.matches("^[a-zA-Z0-9 ]+$")) {
            throw new IllegalArgumentException("The unit of the address must only contain alphanumeric characters.");
        }
        this.unit = unit;
        hash = null;
    }

    /**
     * Sets the state abbreviation.
     *
     * <p>The value must be one of the 50 US state abbreviations listed in
     * {@code VALID_STATES}. Note that {@link Array#contains(Object)}
     * compares by reference, so only the exact same {@code String} instances
     * (such as interned literals) are recognized as valid.</p>
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param state the state abbreviation
     * @throws AssertionError          if {@code state} is {@code null}
     * @throws IllegalArgumentException if {@code state} is not a valid state
     *                                  abbreviation
     */
    public void setState(String state) {
        assert state != null;
        if (!VALID_STATES.contains(state)) {
            throw new IllegalArgumentException("Must be a valid state.");
        }
        this.state = state;
        hash = null;
    }

    /**
     * Sets the zip code.
     *
     * <p>The value must contain only numeric characters; no length or range
     * is enforced.</p>
     *
     * Time Complexity: O(m) where m is the length of the given zip.
     * Space Complexity: O(1).
     *
     * @param zip the zip code
     * @throws AssertionError          if {@code zip} is {@code null}
     * @throws IllegalArgumentException if {@code zip} contains non-numeric
     *                                  characters
     */
    public void setZip(String zip) {
        assert zip != null;
        if (!zip.matches("^\\d+$")) {
            throw new IllegalArgumentException("Must only contain numeric characters.");
        }
        this.zip = zip;
        hash = null;
    }

    /**
     * Sets the first name.
     *
     * <p>The value is stripped of leading and trailing whitespace, must not
     * be empty, and is truncated to 35 characters.</p>
     *
     * Time Complexity: O(m) where m is the length of the given name.
     * Space Complexity: O(1).
     *
     * @param name the first name
     * @throws AssertionError          if {@code name} is {@code null}
     * @throws IllegalArgumentException if {@code name} is empty after
     *                                  stripping whitespace
     */
    public void setFirstName(String name) {
        assert name != null;
        name = name.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty.");
        }
        this.firstName = name.substring(0, Math.min(35, name.length()));
        hash = null;
    }

    /**
     * Sets the middle name.
     *
     * <p>The value is stripped of leading and trailing whitespace and
     * truncated to 35 characters. A {@code null} or blank value is ignored,
     * leaving any previous middle name unchanged.</p>
     *
     * Time Complexity: O(m) where m is the length of the given name.
     * Space Complexity: O(1).
     *
     * @param name the middle name, or {@code null} to leave it unchanged
     */
    public void setMiddleName(String name) {
        if (name == null) {
            return;
        }
        name = name.strip();
        if (name.isEmpty()) {
            return;
        }
        this.middleName = name.substring(0, Math.min(35, name.length()));
        hash = null;
    }

    /**
     * Sets the last name.
     *
     * <p>The value is stripped of leading and trailing whitespace, must not
     * be empty, and is truncated to 35 characters.</p>
     *
     * Time Complexity: O(m) where m is the length of the given name.
     * Space Complexity: O(1).
     *
     * @param name the last name
     * @throws AssertionError          if {@code name} is {@code null}
     * @throws IllegalArgumentException if {@code name} is empty after
     *                                  stripping whitespace
     */
    public void setLastName(String name) {
        assert name != null;
        name = name.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty.");
        }
        this.lastName = name.substring(0, Math.min(35, name.length()));
        hash = null;
    }

    /**
     * Sets the social security number.
     *
     * <p>The value must match the {@code XXX-XX-XXXX} format.</p>
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param ssn the ssn
     * @throws AssertionError          if {@code ssn} is {@code null}
     * @throws IllegalArgumentException if {@code ssn} does not match
     *                                  {@code XXX-XX-XXXX}
     */
    public void setSsn(String ssn) {
        assert ssn != null;
        if (!ssn.matches("^\\d{3}-\\d{2}-\\d{4}$")) {
            throw new IllegalArgumentException("SSN does not match required format: XXX-XX-XXXX");
        }
        this.ssn = ssn;
        hash = null;
    }

    /**
     * Determines whether this user is equal to the given object.
     *
     * <p>Two users are equal when they are the same instance, or when they
     * are both {@code User} objects with equal values for first name, middle
     * name, last name, street, unit, state, zip and ssn.</p>
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param obj the object to compare with
     * @return {@code true} if the objects are equal, otherwise {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        // 1. Reference check
        if (this == obj) return true;

        // 2. Null and type check
        if (obj == null || getClass() != obj.getClass()) return false;

        // 3. Cast
        User other = (User) obj;

        // 4. Comparison
        return Objects.equals(this.getFirstName(), other.getFirstName())
                && Objects.equals(this.getMiddleName(), other.getMiddleName())
                && Objects.equals(this.getLastName(), other.getLastName())
                && Objects.equals(this.getStreet(), other.getStreet())
                && Objects.equals(this.getUnit(), other.getUnit())
                && Objects.equals(this.getState(), other.getState())
                && Objects.equals(this.getZip(), other.getZip())
                && Objects.equals(this.getSsn(), other.getSsn());
    }

    /**
     * Returns the hash code of this user.
     *
     * <p>The hash code is computed from all eight fields and cached; it is
     * invalidated (recomputed on next call) whenever a setter modifies the
     * user.</p>
     *
     * Time Complexity: O(1) amortized (cached after the first computation).
     * Space Complexity: O(1).
     *
     * @return the cached hash code
     */
    @Override
    public int hashCode() {
        if (hash == null) {
            hash = Objects.hash(firstName, middleName, lastName, street, unit, state, zip, ssn);
        }
        return hash;
    }

}
