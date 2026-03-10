import React, { useEffect } from "react";
import { FallbackProps } from "react-error-boundary";
import { Button } from "react-bootstrap";
import { useNavigate } from "react-router";

export const ErrorFallback: React.FC<FallbackProps> = ({
  error,
  resetErrorBoundary,
}) => {
  const navigate = useNavigate();

  useEffect(() => {
    console.log(
      `Unexpected error: ${error instanceof Error ? error.message : error}`,
    );
  }, [error]);

  return (
    <div role={"alert"}>
      <h3>Whoops! Something went wrong!</h3>
      <Button
        className="mr-2"
        onClick={() => {
          resetErrorBoundary();
          navigate(-1);
        }}
      >
        Go back
      </Button>
      <Button
        variant="secondary"
        onClick={() => {
          resetErrorBoundary();
        }}
      >
        Retry
      </Button>
    </div>
  );
};
