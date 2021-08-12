import React, { Dispatch, SetStateAction, useContext, useState } from "react";

interface JWTPayload {
  iss?: string;
  sub?: string;
  aud?: string;
  exp?: number;
  iat?: number;
  auth_time?: number;
  nonce?: string;
  amr?: string[];
  name?: string;
  picture?: string;
  email?: string;
}
export type PathPattern = "auth" | "code";
export type GlobalState = {
  value: number;
  loggedIn: boolean;
  idToken?: JWTPayload;
  displayName?: string;
  logText: string;
  pathPattern: PathPattern;
};

const initialState: GlobalState = {
  value: 0,
  loggedIn: false,
  logText: "",
  pathPattern: "auth",
};

const AppStateContext = React.createContext<GlobalState>(initialState);
const SetAppStateContext = React.createContext<
  Dispatch<SetStateAction<GlobalState>>
>(() => {});

export function useGlobalState() {
  return useContext(AppStateContext);
}
export function useSetGlobalState() {
  return useContext(SetAppStateContext);
}

export function GlobalStateProvider(props: {
  initialState?: GlobalState;
  children: React.ReactNode;
}) {
  const [state, setState] = useState<GlobalState>(
    props.initialState ?? initialState
  );
  return (
    <AppStateContext.Provider value={state}>
      <SetAppStateContext.Provider value={setState}>
        {props.children}
      </SetAppStateContext.Provider>
    </AppStateContext.Provider>
  );
}
