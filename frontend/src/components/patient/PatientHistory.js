import React from "react";
import { FormattedMessage, useIntl, injectIntl } from "react-intl";
import "../Style.css";
import { Heading, Grid, Column, Section, Breadcrumb, BreadcrumbItem, } from "@carbon/react";
import SearchPatientForm from "./SearchPatientForm";
import { useState, useEffect, useRef } from "react";

const PatientHistory = () => {
  const intl = useIntl();
  const [selectedPatient, setSelectedPatient] = useState({});
  const componentMounted = useRef(false);

  const getSelectedPatient = (patient) => {
    if (componentMounted.current) {
      setSelectedPatient(patient);
    }
  };

  const [newPatientTab, setNewPatientTab] = useState({
    kind: "tertiary",
    active: false,
  });

  useEffect(() => {
    componentMounted.current = true;
    openPatientResults(selectedPatient.patientPK);

    return () => {
      componentMounted.current = false;
    };
  }, [selectedPatient]);

  const openPatientResults = (patientId) => {
    if (patientId) {
      window.location.href = "/PatientResults/" + patientId;
    }
  };

  return (
    <>
    <Grid fullWidth={true}>
      <Column lg={16}>
        <Breadcrumb>
          <BreadcrumbItem href="/">
            {intl.formatMessage({id:"home.label"})}
          </BreadcrumbItem>
          {newPatientTab.active && (
              <BreadcrumbItem href="/PatientHistory">
                {intl.formatMessage({
                  id: "label.page.patientHistory",
                })}
              </BreadcrumbItem>
            )}
        </Breadcrumb>
      </Column>
    </Grid>
      <Grid fullWidth={true}>
        <Column lg={16}>
          <Section>
            <Section>
              <Heading>
                <FormattedMessage id="label.page.patientHistory" />
              </Heading>
            </Section>
          </Section>
        </Column>
      </Grid>
      <br></br>

      <div className="orderLegendBody">
        <SearchPatientForm getSelectedPatient={getSelectedPatient} />
      </div>
    </>
  );
};
export default injectIntl(PatientHistory);
