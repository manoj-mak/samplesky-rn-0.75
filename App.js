import React from 'react';

import VisitRnSdkView from 'react-native-visit-rn-sdk';

import {EventRegister} from 'react-native-event-listeners';

import {SafeAreaView, Alert} from 'react-native';

const visitBaseUrl = 'https://insurance-uat.getvisitapp.net/wonderwomen';

const errorBaseUrl = 'https://star-health.getvisitapp.net/';

const token =
  '78ftP8oDeLCCdBp8QH5d/o7QvxeFBcBYT1nPtn/LgVQ3CW4R+xE5kSZJZvtNcfTwtOeD2aSoxjcjoJcaiSFY1CjyuH2qAHG9JX888hFKQXutxbumqxYaGZjK4nUg2Te6JxfNTlWjFk8/cPNI6JWJa7kKYYcV7xssd63oLvTP9sks42cU/XbCFeXMXHmyCtXC2vV2aOiVNAjZytzsBuHgWhP5LKzGwcCIvRzmoT1DSOgnow+Ccf1uvAvva4krj41JWLGNugqH097BW5jcbVlpCY3xMBKoPn3mWTIblFtZaujOIZY4a2qjjOYuDy2xrx1aLytiKnmTe18Af6Br9qxp7yZQ0NmNrp2rIYfrJvB+T1Itzn19hQ0cOIfrAIUvQbseFhFUN3IEj8ifc6lpeG5R9XpII4jsVBWuo/i/9xuMKVz1Um0dL9E2mREEI+bEzdOSGjOF7CzjXdOF3hVi3xe6ESP/LNtI28ORrf/4juWj4wc2v+5Auhqi0jEkNTCXqcWQm+SbKqBYJxaw7m5A3HUx/uLUmhJLujHn25r8cVXTxO+SugooP/EygzzEsiPvpnqMER0Suze1YBU4rWjeHrkZGYlcMNO1sm2LjmzFe+SSDt7X6EplduwdKZtnDydnP3QNR0fiOKqfcItZ6yKNJ6Am1JVceNfFiR0+LX+NdLLi90pUfbaO3cKuQvxwTW/ann6Uki739ob8IYj44bPQB9COG2BRbjgwUOr2FrJJRXPwPTI0douUPkLzc0s1H3glG2c2CwCfRxuAvRCAh6DYIKxIhBnHh1kF5TLA6D4KK3SkbdSApFYs6KdWp6yQf00X3whYFRFUIoT46JJzYfK45G7c+oecV228koQbJEGVOVXdlFI0cOyOyxXvIG3lbTm78YHhWTlFChoSOVoWfGUI91pFJOoa4YGSbnVij8PqMn+1oPV3ytRPJVOSwQRFKDUZhFOGkcfj+xDAOJpdU8BrFrf4XRpf9aXfjqCcsEwBp5fwfKLstVyU40g6reRCg5YtR0QE4YmUTjl0RX4AYFSTANabKsnJnLkd7ELJQbOV6PlE5IhEYqFAM2tB1yFrWh138ff/aPVEXS5Z18YinXq4VxyIqcqGOA4qp0Zf5LQeF6hJ3M2MnRp3OJTITBk64ns8FibnzpwHmuunxKhfqJT9ukGVVISfLr8RuFHbM89HMpCPgNrL5r/G3mRRwfi+d9PUaewYvxZYTdaeS//2t4+p/LNwxNeNMbbqIEJbb8iVb61hTUGidIlE4LaA40NxZPTx3rzj5cR6QOGXFelD0ChnWyidH71YEwsnGpCiHqeTPex2ibzHV+8UccamlLsUAHL44BjA4ytarehkXv+XEGhO+gIwq55xQqY7vWes/PGfZKnRGonfQUrb58Z14lJ90XVsosbkyorYp8R0EJ9yTM8+Kuxqr3UYfDpnHFdWjW5BhYwvjjT5O+sWUD/GYEdxEMi7tyG3BmHH/i0zaUKE2QS7piLFVCK8FqNkvQXqZ8/OLVfUf8RWVoS1kAElfzaPGIYT7ucKn4hmhj/dM5jI5ts+Tj3u+/24j3Zxu5LvEeyFfULSiCOwzcPYtXAryAQdVu/DtbgYbEDVnlguKzb73voJy5P4RBcEm3K+qwOThTfA42nQaMx3suF52GHrJ+lDVKRTiNpF7pyculnmdt8CQ1WLyLVFTf3EBFGxRt6K6zXYdQGeg4Poa5ShlP2aCzT9fBElpTuC9VAtW81pMOJKUVcsJbRmGO9kM6kcc6FgxVaVWxZpoQ+Ltz4284M9M8jW33kKhsJalxC5zUlVb4vJcxzqv40m0E9QE7UrK3icfdNZPcAjueGvBQyHsw1oKMqeZPejrBIvH0SCTaB6IsYWCNdtxUV9brriUak1D8jJoh8Cj2ubjaD9wCIvy5aw7w/HyfPkhK5rwbSO1ZflB1X0kR2YnqFV/sN1opR8PmEXisGsYBT+38E=';

const moduleName = 'pharmacy';
const visitEvent = 'visit-event';

function App() {
  React.useEffect(() => {
    const listener = EventRegister.addEventListener(visitEvent, data => {
      if (data.message === 'unauthorized-wellness-access') {
        Alert.alert('Error', data.errorMessage);
      } else if (data.message === 'generate-magic-link-failed') {
        console.log('magic link failed. ', data);
        //add analytics event to track for whom this is happening.
      } else if (data.message === 'getDeviceInfo-failed') {
        console.log('device Info library failed', data);
        //add analytics event to track for whom this is happening.
      } else if (data.message === 'web-view-error') {
        console.log('web-view-error', data.errorMessage);
        //when webview throws error.
      } else if (data.message === 'external-server-error') {
        Alert.alert('Error', data.errorMessage);
      } else if (data.message === 'health-connect-error-event') {
        //when something goes wrong in health connect connection or data retrival.
        //log this your analytics or firebase console.
      }
    });
    return () => {
      EventRegister.removeEventListener(listener);
    };
  }, []);

  return (
    // eslint-disable-next-line react-native/no-inline-styles
    <SafeAreaView style={{flex: 1}}>
      {/* <Button
        title="Press me"
        color="#f194ff"
        onPress={() => Alert.alert('Button with adjusted color pressed')}
      /> */}

      <VisitRnSdkView
        baseUrl={visitBaseUrl}
        errorBaseUrl={errorBaseUrl}
        token={token}
        cpsid={''}
        moduleName={moduleName}
        environment={'sit'}
        isLoggingEnabled={true}
        magicLink={
          'https://star-health.getvisitapp.com/?code=Z2je9q9B&pid=2971268'
        }
      />
    </SafeAreaView>
  );
}

export default App;
