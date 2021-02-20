import React from "react";
import "@testing-library/jest-dom";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { AddOn } from "./AddOn";

jest.mock("./LegacyFaIcon", () => ({
  LegacyFaIcon: ({ icon }) => <span>{icon}</span>,
}));

const mockAddOn = {
  uid: "my-addon",
  name: "My AddOn",
};

const mockAddOnWithDescription = {
  ...mockAddOn,
  description: "My Test AddOn",
};

const mockAddOnWithIcon = {
  ...mockAddOn,
  icon: "checkbox",
};

describe("<AddOn/>", () => {
  it("should render a shell without an addOn", () => {
    const { container } = render(<AddOn />, { wrapper: MemoryRouter });
    expect(container.querySelector("*")).toBeInTheDocument();
    expect(screen.queryByDisplayValue(/.*/)).not.toBeInTheDocument();
  });

  it("should render a simple addOn", () => {
    render(<AddOn addOn={mockAddOn} />, { wrapper: MemoryRouter });
    expect(screen.queryByText("My AddOn")).toBeVisible();

    expect(screen.getByText("My AddOn").closest("a")).toHaveAttribute(
      "href",
      `/show/${mockAddOn.uid}`
    );
  });

  it("should render a link for the specific version", () => {
    render(<AddOn addOn={mockAddOn} version="0.0.0" />, {
      wrapper: MemoryRouter,
    });
    expect(screen.getByText("My AddOn").closest("a")).toHaveAttribute(
      "href",
      `/show/${mockAddOn.uid}?highlightVersion=0.0.0`
    );
  });

  it("should render the addOn's description", () => {
    render(<AddOn addOn={mockAddOnWithDescription} />, {
      wrapper: MemoryRouter,
    });

    expect(screen.queryByText("My Test AddOn")).toBeVisible();
  });

  it("should render the addOn's icon if defined", () => {
    render(<AddOn addOn={mockAddOnWithIcon} />, {
      wrapper: MemoryRouter,
    });

    expect(screen.queryByText(mockAddOnWithIcon.icon)).toBeVisible();
  });
});
