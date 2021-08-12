import * as React from "react";
import * as ReactDOM from "react-dom";
import LoadingOverlay from "react-loading-overlay-ts";
import VConsole from "vconsole";
import {
  GlobalStateProvider,
  PathPattern,
  useGlobalState,
  useSetGlobalState,
} from "./contexts/GlobalStateContext";
import { LineLogin } from "./components/LineLogin";
import { hrefSpotify } from "./util/value";
import liff from "@line/liff/dist/lib";

new VConsole({ maxLogNumber: 1000 });

const InitialLoginApp: React.FC = () => {
  const state = useGlobalState();
  return (
    <div>
      <LineLogin />
      <div>{state.loggedIn && <h1>{"ログインしました"}</h1>}</div>
      <div
        style={{
          marginTop: "5ex",
        }}
      >
        <a
          className="link"
          style={{
            color: "white",
            backgroundColor: "#20AE43",
            padding: "2ex",
            fontSize: "2.4ex",
            borderRadius: "40px",
            textDecoration: "none",
            fontFamily: "sans-serif",
          }}
          href={hrefSpotify}
        >
          spotify 連携
        </a>
      </div>
    </div>
  );
};

interface SwitcherProp {
  pathPattern: PathPattern;
}
const Switcher: React.FC<SwitcherProp> = (prop) => {
  const state = useGlobalState();
  const setGlobalState = useSetGlobalState();
  React.useEffect(() => {
    (async () => {
      const queryString = window.location.search;
      const urlParams = new URLSearchParams(queryString);
      const code = urlParams.get("code");
      if (code && state.idToken?.iss) {
        console.log("code が存在した！");
        setGlobalState((s) => ({
          ...s,
          pathPattern: "code",
        }));
        const data = {
          ...state.idToken,
          code: code,
        };
        console.log(`fetch /post を実行...`);
        const result = await fetch("/post", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(data),
        }).catch((e) => {
          const ej = JSON.stringify(e);
          console.log(`[fetch error] e: ${e}, ej: ${ej}`);
        });
        console.log(`result: ${JSON.stringify(result)}`);
        liff.closeWindow();
      }
    })();
  }, [window.location, state.idToken]);
  return <></>;
};

const App: React.FC = () => {
  const state = useGlobalState();

  React.useEffect(() => {
    console.log(`stateが変化しました in app : ${JSON.stringify(state)}`);
  }, [state]);
  return (
    <LoadingOverlay
      active={!state.loggedIn}
      spinner
      text="Logging in with LINE..."
      className="loading"
    >
      <Switcher pathPattern={state.pathPattern} />
      <InitialLoginApp />
    </LoadingOverlay>
  );
};

const Main = () => {
  return (
    <GlobalStateProvider>
      <App />
    </GlobalStateProvider>
  );
};

ReactDOM.render(<Main />, document.querySelector("#app"));
