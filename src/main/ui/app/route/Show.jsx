/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import React, { useContext, useEffect, useMemo, useState } from "react";
import { useParams } from "react-router";
import { Link } from "react-router-dom";
import {
  Badge,
  Button,
  Col,
  OverlayTrigger,
  Row,
  Table,
  Tooltip,
} from "react-bootstrap";
import { useQuery } from "react-query";
import ReactGA from "react-ga";

import dayjs from "dayjs/esm";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faDownload } from "@fortawesome/free-solid-svg-icons";
import { faQuestionCircle } from "@fortawesome/free-regular-svg-icons";

import { CoreVersionContext } from "../App";
import { useSearchParams } from "../hooks";
import { ExternalLink, LegacyFaIcon } from "../component";
import { myFetch } from "../utils";

const DownloadButton = ({ uid, version, children, ...props }) => {
  return (
    <Button
      onClick={
        GA_ID
          ? () => {
              ReactGA.event({
                category: "User",
                action: "DownloadAddOn",
                label: uid,
              });

              version &&
                ReactGA.event({
                  category: "User",
                  action: "DownloadAddOnVersion",
                  label: `${uid}:${version}`,
                });
            }
          : null
      }
      {...props}
    >
      {children}
    </Button>
  );
};

const formatLinkHeader = (link) => {
  switch (link.rel) {
    case "source":
      return "Source Code";
    case "documentation":
      return "Documentation";
    default:
      return link.rel;
  }
};

const formatDateTime = (dt) => {
  if (!!!dt) {
    return (
      <>
        <br />
        <br />
      </>
    );
  }

  const m = dayjs(dt);

  if (!!!m) {
    return (
      <>
        <br />
        <br />
      </>
    );
  }

  return (
    <>
      {m.fromNow()}
      <br />
      {m.format("ll")}
    </>
  );
};
const formatRequiredModules = (version) => {
  const requirements = [];

  if (version.requireModules) {
    version.requireModules.forEach((m) => {
      requirements.push(
        `${m.module.replace("org.openmrs.module.", "")} ${m.version || ""}`
      );
    });
  }

  return requirements.join(", ");
};

export const Show = () => {
  const { uid } = useParams();
  const { highlightVersion } = useSearchParams();
  const coreVersion = useContext(CoreVersionContext);

  const addOnResult = useQuery(
    ["addOn", uid],
    () => myFetch(`/api/v1/addon/${uid}`),
    { enabled: !!uid }
  );

  const addOn = useMemo(() => addOnResult.data, [addOnResult.data]);

  const latestVersionResult = useQuery(
    ["addOnLatestVersion", coreVersion],
    () =>
      myFetch(
        `/api/v1/addon/${uid}/latestVersion` +
          (coreVersion ? `?coreversion=${coreVersion}` : "")
      ),
    { enabled: !!uid }
  );

  const [latestVersion, setLatestVersion] = useState(latestVersionResult.data);

  useEffect(() => {
    if (latestVersionResult.data) {
      setLatestVersion(latestVersionResult.data);
    }
  }, [latestVersionResult.data]);

  const tag = useMemo(() => {
    if (!!!addOn) {
      return null;
    }

    let variant = "primary";
    switch (addOn.status) {
      case "ACTIVE":
        variant = "success";
        break;
      case "INACTIVE":
        variant = "warning";
        break;
      case "DEPRECATED":
        variant = "danger";
        break;
    }

    return (
      <>
        <Badge variant={variant}>{addOn.status}</Badge>&nbsp;
        {addOn.tags
          ? addOn.tags.map((t) => (
              <Badge
                key={t}
                variant={"secondary"}
                as={"a"}
                href={`/search?&tag=${t}`}
              >
                {t}
              </Badge>
            ))
          : null}
      </>
    );
  }, [addOn]);

  const hostedAtRow = useMemo(() => {
    if (!addOn || !addOn.hostedUrl) {
      return null;
    }

    const hosted = addOn.hostedUrl.includes("bintray.com") ? (
      <ExternalLink link={{ title: "Bintray", href: addOn.hostedUrl }} />
    ) : (
      <ExternalLink link={{ href: addOn.hostedUrl }} />
    );

    return (
      <tr>
        <th>Hosted at</th>
        <td>{hosted}</td>
      </tr>
    );
  }, [addOn]);

  const version = useMemo(() => {
    if (latestVersion && addOn && addOn.versions) {
      if (latestVersion.version === addOn.versions[0].version) {
        return (
          <span>
            Download <FontAwesomeIcon icon={faDownload} />
            <br />
            <small>Latest Version: {latestVersion.version}</small>
          </span>
        );
      } else {
        return (
          <span>
            Download <FontAwesomeIcon icon={faDownload} />
            <br />
            <small>Supported Version: {latestVersion.version}</small>
          </span>
        );
      }
    } else {
      return (
        <span>
          No module version supports
          <br />
          <small>OpenMRS Core {coreVersion}</small>
        </span>
      );
    }
  }, [latestVersion, coreVersion, addOn]);

  const versionDownloadUri = latestVersion ? latestVersion.downloadUri : null;

  if (addOnResult.isLoading) {
    return <></>;
  }

  if (addOnResult.isError) {
    if (addOnResult.error.status === 404) {
      return (
        <>
          Sorry! We couldn't find the module "{uid}".
          <br />
          <Link to={"/"}>Go back to home screen</Link>
        </>
      );
    } else {
      return <></>;
    }
  }

  return (
    <>
      <Row style={{ marginBottom: `1rem` }} sm={12}>
        <Col sm={1} className="hidden-xs">
          <LegacyFaIcon icon={addOn.icon} size={"3x"} />
        </Col>
        <Col sm={11}>
          <h2>{addOn.name}</h2>
          <h4 className="lead">{addOn.description}</h4>
          <div>{tag}</div>
        </Col>
      </Row>
      <Row xs={12}>
        <Col xs={9}>
          <Table variant="condensed">
            <tbody>
              <tr>
                <th>Type</th>
                <td>{addOn.type}</td>
              </tr>
              {hostedAtRow}
              <tr>
                <th>Maintained by</th>
                <td>
                  <>
                    {addOn?.maintainers?.map((m) =>
                      m.url ? (
                        <ExternalLink key={m.name} link={{ href: m.url }}>
                          <span className="maintainer">{m.name}</span>
                        </ExternalLink>
                      ) : (
                        <span key={m.name} className="maintainer">
                          {m.name}
                        </span>
                      )
                    )}
                  </>
                </td>
              </tr>
              {addOn?.links?.map((l) => (
                <tr key={l.rel}>
                  <th>{formatLinkHeader(l)}</th>
                  <td>
                    <ExternalLink link={l} />
                  </td>
                </tr>
              ))}
              <tr>
                <th>Downloads in the last 30 days</th>
                <td>{addOn?.downloadCountInLast30Days || "?"}</td>
              </tr>
            </tbody>
          </Table>
        </Col>
        <Col xs={3}>
          <DownloadButton
            uid={addOn.uid}
            version={latestVersion?.version}
            variant="primary"
            size="lg"
            disabled={versionDownloadUri === null}
            href={
              latestVersion.renameTo
                ? `/api/v1/addon/${addOn.uid}/${latestVersion.version}/download`
                : latestVersion.downloadUri
            }
          >
            {version}
          </DownloadButton>
        </Col>
      </Row>
      <>
        <Table variant="condensed" hover>
          <thead>
            <tr>
              <th>Version</th>
              <th>Release Date</th>
              <th>
                Platform Requirements&nbsp;
                <OverlayTrigger
                  placement="right"
                  overlay={
                    <Tooltip id="tooltip">
                      <strong>
                        Minimum version of the OpenMRS Platform required.
                      </strong>
                    </Tooltip>
                  }
                >
                  <LegacyFaIcon icon={faQuestionCircle} />
                </OverlayTrigger>
              </th>
              <th>Other requirements</th>
              <th>Download</th>
            </tr>
          </thead>
          <tbody>
            {addOn?.versions?.map((v) => {
              const className =
                v.version === highlightVersion ? "highlight" : null;
              return (
                <tr key={v.version} className={className}>
                  <td>{v.version}</td>
                  <td>{formatDateTime(v.releaseDatetime)}</td>
                  <td>{v.requireOpenmrsVersion}</td>
                  <td>{formatRequiredModules(v)}</td>
                  <td>
                    <DownloadButton
                      variant="outline-primary"
                      size="sm"
                      href={
                        v.renameTo
                          ? `/api/v1/addon/${addOn.uid}/${v.version}/download`
                          : v.downloadUri
                      }
                    >
                      <FontAwesomeIcon icon={faDownload} />
                      Download
                    </DownloadButton>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </Table>
      </>
    </>
  );
};
